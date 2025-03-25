package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

import com.intellij.debugger.ui.DebuggerContentInfo;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.impl.ViewImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.content.Content;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XDropFrameHandler;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.mayreh.intellij.plugin.tlaplus.run.debugger.DebuggerMessage.CapabilitiesEvent;
import com.mayreh.intellij.plugin.tlaplus.run.debugger.DebuggerMessage.StoppedEvent;

public class TLCDebugProcess extends XDebugProcess {
    private static final Logger log = Logger.getInstance(TLCDebugProcess.class);
    private static final Pattern VIOLATION_PATTERN = Pattern.compile("^Invariant.*is violated.$");

    private final ExecutionResult executionResult;
    private final ServerConnection serverConnection;
    private final ExecutorService executorService;
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final AtomicBoolean attractedOnce = new AtomicBoolean(false);
    private final TLCDropFrameHandler dropFrameHandler;
    private final TLCBreakpointHandler breakpointHandler;
    private final TLCExceptionBreakpointHandler exceptionBreakpointHandler;

    public TLCDebugProcess(@NotNull XDebugSession session,
                           BlockingQueue<DebuggerMessage> messageQueue,
                           ServerConnection serverConnection,
                           ExecutionResult executionResult) {
        super(session);
        this.executionResult = executionResult;
        this.serverConnection = serverConnection;
        dropFrameHandler = new TLCDropFrameHandler(serverConnection);
        dropFrameHandler.setCanDrop(false);
        breakpointHandler = new TLCBreakpointHandler(session, serverConnection);
        exceptionBreakpointHandler = new TLCExceptionBreakpointHandler(session, serverConnection);
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread th = new Thread(r);
            th.setName(String.format("TLCDebuggerMessagePoller-%d", System.identityHashCode(this)));
            return th;
        });
        executorService.execute(() -> {
            while (!terminated.get()) {
                try {
                    handleMessage(session, messageQueue.take());
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    log.error("Error while handling message. Debugger may no longer works", e);
                    throw new RuntimeException(e);
                }
            }
        });

        // We don't set any capability flags because TLC debugger doesn't check them at all
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        initArgs.setAdapterID("tlaplus-intellij-plugin");
        serverConnection.sendRequest(remoteProxy -> {
            remoteProxy.initialize(initArgs).thenAccept(cap -> {
                dropFrameHandler.setCanDrop(cap.getSupportsStepBack());
                exceptionBreakpointHandler.createOrRemoveBreakpoints(cap);
                // Command line args for TLC debugger are passed when starting the process as usual
                // rather than launch request, so we don't need to pass any args here.
                remoteProxy.launch(Map.of());
            });
        });
    }

    private void handleMessage(XDebugSession session, DebuggerMessage message) {
        log.debug("Received message: " + message);

        if (message instanceof DebuggerMessage.InitializedEvent) {
            ApplicationManager.getApplication().runReadAction(session::initBreakpoints);
            serverConnection.sendRequest(remoteProxy -> {
                remoteProxy.configurationDone(new ConfigurationDoneArguments());
            });
        } else if (message instanceof DebuggerMessage.StoppedEvent) {
            // When debugger suspends by any reason, we no longer need run-to-cursor breakpoint.
            // - Case when run-to-cursor is reached: of course we don't need it anymore.
            // - Case when other breakpoint hits before reaching run-to-cursor:
            //     If we don't clean up, it might be hit later unexpectedly because
            //     the breakpoint is not shown in anywhere, so need clean up.
            breakpointHandler.unregisterRunToCursor();
            StoppedEventArguments stoppedEventArgs = ((StoppedEvent) message).args();

            serverConnection.sendRequest(remoteProxy -> {
                remoteProxy.threads().thenAccept(response -> {
                    Arrays.stream(response.getThreads())
                          .filter(t -> t.getId() == stoppedEventArgs.getThreadId())
                          .findFirst()
                          .ifPresent(thread -> {
                              StackTraceArguments args = new StackTraceArguments();
                              args.setThreadId(thread.getId());
                              remoteProxy.stackTrace(args).thenAccept(stackTraceResponse -> {
                                  XSuspendContext xContext = session.getSuspendContext();
                                  if (xContext == null) {
                                      xContext = new TLCSuspendContext(serverConnection);
                                  }
                                  if (xContext instanceof TLCSuspendContext) {
                                      ((TLCSuspendContext) xContext).addExecutionStack(thread, Arrays.asList(stackTraceResponse.getStackFrames()));
                                  }

                                  if (StoppedEventArgumentsReason.EXCEPTION.equals(stoppedEventArgs.getReason())) {
                                      // FIXME: This logic to lookup the hit breakpoint is clearly a hack.
                                      // Ideally, TLCDebugger should fill StoppedEventArguments#hitBreakpointIds so
                                      // we can look up hit exception breakpoint correctly.
                                      // Whenever we fail to lookup breakpoint, we just call positionReached instead of breakpointReached.
                                      //
                                      // refs: filter IDs are defined in https://github.com/tlaplus/tlaplus/blob/a649facaac46a79c86b664073920e9762a5c6f0a/tlatools/org.lamport.tlatools/src/tlc2/debug/TLCDebugger.java#L248-L278
                                      if (stoppedEventArgs.getText() != null &&
                                          VIOLATION_PATTERN.matcher(stoppedEventArgs.getText().trim()).matches()) {
                                          Optional<XBreakpoint<TLCExceptionBreakpointProperties>> breakpoint =
                                                  exceptionBreakpointHandler.findBreakpointByFilterId("InvariantBreakpointsFilter");
                                          if (breakpoint.isPresent()) {
                                              session.breakpointReached(breakpoint.get(), null, xContext);
                                              return;
                                          }
                                      }
                                      Optional<XBreakpoint<TLCExceptionBreakpointProperties>> breakpoint =
                                              exceptionBreakpointHandler.findBreakpointByFilterId("ExceptionBreakpointsFilter");
                                      if (breakpoint.isPresent()) {
                                          session.breakpointReached(breakpoint.get(), null, xContext);
                                      } else {
                                          // Just a defensive fallback. So we don't need to focus frames tab.
                                          // See below for the case we want to focus frames tab.
                                          session.positionReached(xContext);
                                      }
                                  } else {
                                      // NOTE: We give up looking-up hit breakpoint (except exception breakpoints above) on stopped event so
                                      // always call positionReached instead of breakpointReached, because
                                      // Some TLCDebugger's breakpoint types (e.g. Spec breakpoint) happen on different line
                                      // from the breakpoint's line, which is hard to look-up without re-implementing
                                      // similar logic with TLCDebugger's halt-condition.
                                      //
                                      // Ideally, TLCDebugger should fill StoppedEventArguments#hitBreakpointIds so
                                      // we can look up hit breakpoint easily.
                                      session.positionReached(xContext);

                                      // NOTE: We want to focus frames tab to get user attention for let them know the debugger session is started.
                                      // However, TLC may send stopped event without breakpoint on first encounter of evaluation after launch,
                                      // and auto-focus is performed only in breakpointReached, not in positionReached.
                                      //
                                      // Though auto-focus can be done by calling XDebugSessionImpl#positionReached(..., boolean attract),
                                      // it's an internal API and not able to use directly.
                                      // So we use a workaround to focus frames tab only once when the first stopped event is received.
                                      if (!attractedOnce.getAndSet(true)) {
                                          focusFramesTab(session);
                                      }
                                  }
                              });
                          });
                });
            });
        } else if (message instanceof DebuggerMessage.TerminatedEvent) {
            // noop
            // terminate sequence is expected to be already ongoing in stop() method
            // so we don't need to do anything here.
        } else if (message instanceof DebuggerMessage.OutputEvent) {
            // noop
            // TODO: Output to dedicated console might be helpful
        } else if (message instanceof DebuggerMessage.CapabilitiesEvent) {
            dropFrameHandler.setCanDrop(((CapabilitiesEvent) message).args().getCapabilities().getSupportsStepBack());
        }
    }

    // Intellij's debugger engine uses the process handler returned from here (e.g. XDebugSessionImpl)
    // to manage the lifecycle of the debug process rather than ExecutionResult's process handler,
    // while default implementation returns dummy-like DefaultDebugProcessHandler.
    // To tell debugger system attach the real process handler, we need to override this method.
    @Override
    protected @Nullable ProcessHandler doGetProcessHandler() {
        return executionResult.getProcessHandler();
    }

    // To show TLC console output view in the debugger tab
    @Override
    public @NotNull ExecutionConsole createConsole() {
        return executionResult.getExecutionConsole();
    }

    @Override
    public @NotNull XDebuggerEditorsProvider getEditorsProvider() {
        return new TLCDebuggerEditorProvider();
    }

    @Override
    public XBreakpointHandler<?> @NotNull [] getBreakpointHandlers() {
        return new XBreakpointHandler<?>[] { breakpointHandler, exceptionBreakpointHandler };
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        org.eclipse.lsp4j.debug.Thread thread = activeThread(context);
        if (thread != null) {
            NextArguments args = new NextArguments();
            args.setThreadId(thread.getId());
            serverConnection.sendRequest(remoteProxy -> remoteProxy.next(args));
        }
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        org.eclipse.lsp4j.debug.Thread thread = activeThread(context);
        if (thread != null) {
            StepInArguments args = new StepInArguments();
            args.setThreadId(thread.getId());
            serverConnection.sendRequest(remoteProxy -> remoteProxy.stepIn(args));
        }
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        org.eclipse.lsp4j.debug.Thread thread = activeThread(context);
        if (thread != null) {
            StepOutArguments args = new StepOutArguments();
            args.setThreadId(thread.getId());
            serverConnection.sendRequest(remoteProxy -> remoteProxy.stepOut(args));
        }
    }

    @Override
    public @Nullable XDropFrameHandler getDropFrameHandler() {
        return dropFrameHandler;
    }

    // DAP launch sequence requires to init breakpoints after initialized event is received
    // before sending configurationDone request.
    // So we call initBreakpoints manually instead of relying on auto call by Intellij's debugger engine.
    @Override
    public boolean checkCanInitBreakpoints() {
        return false;
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        breakpointHandler.registerRunToCursor(position);
        org.eclipse.lsp4j.debug.Thread thread = activeThread(context);
        if (thread != null) {
            ContinueArguments args = new ContinueArguments();
            args.setThreadId(thread.getId());
            serverConnection.sendRequest(remoteProxy -> remoteProxy.continue_(args));
        }
    }

    @Override
    public void stop() {
        terminated.set(true);
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        serverConnection.close();
    }

    @Override
    public @NotNull Promise<Object> stopAsync() {
        return super.stopAsync();
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        org.eclipse.lsp4j.debug.Thread thread = activeThread(context);
        if (thread != null) {
            ContinueArguments args = new ContinueArguments();
            args.setThreadId(thread.getId());
            serverConnection.sendRequest(remoteProxy -> remoteProxy.continue_(args));
        }
    }

    private static void focusFramesTab(XDebugSession session) {
        ApplicationManager.getApplication().invokeLater(() -> {
            RunnerLayoutUi ui = session.getRunContentDescriptor().getRunnerLayoutUi();
            if (ui == null) {
                return;
            }
            Content framesTab = null;
            for (Content content : ui.getContents()) {
                if (DebuggerContentInfo.FRAME_CONTENT.equals(content.getUserData(ViewImpl.ID))) {
                    framesTab = content;
                    break;
                }
            }
            if (framesTab != null) {
                ui.selectAndFocus(framesTab, true, false);
            }
        });
    }

    private static @Nullable org.eclipse.lsp4j.debug.Thread activeThread(XSuspendContext context) {
        if (context instanceof TLCSuspendContext) {
            return ((TLCSuspendContext) context).activeThread();
        }
        return null;
    }
}
