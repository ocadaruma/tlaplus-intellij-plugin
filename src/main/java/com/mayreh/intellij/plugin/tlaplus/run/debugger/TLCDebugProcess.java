package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XDropFrameHandler;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import com.mayreh.intellij.plugin.tlaplus.run.debugger.DebuggerMessage.CapabilitiesEvent;
import com.mayreh.intellij.plugin.tlaplus.run.debugger.DebuggerMessage.StoppedEvent;

public class TLCDebugProcess extends XDebugProcess {
    private static final Logger log = Logger.getInstance(TLCDebugProcess.class);

    private final ExecutionResult executionResult;
    private final IDebugProtocolServer remoteProxy;
    private final ExecutorService executorService;
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final TLCDropFrameHandler dropFrameHandler;
    private final TLCBreakpointHandler breakpointHandler;
    private final TLCExceptionBreakpointHandler exceptionBreakpointHandler;

    public TLCDebugProcess(@NotNull XDebugSession session,
                           BlockingQueue<DebuggerMessage> messageQueue,
                           IDebugProtocolServer remoteProxy,
                           ExecutionResult executionResult) {
        super(session);
        this.executionResult = executionResult;
        this.remoteProxy = remoteProxy;
        dropFrameHandler = new TLCDropFrameHandler(remoteProxy);
        dropFrameHandler.setCanDrop(false);
        breakpointHandler = new TLCBreakpointHandler(session, remoteProxy);
        exceptionBreakpointHandler = new TLCExceptionBreakpointHandler(session, remoteProxy);
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread th = new Thread(r);
            th.setName(String.format("TLCDebuggerMessagePoller-%d", System.identityHashCode(this)));
            return th;
        });
        executorService.execute(() -> {
            while (!terminated.get()) {
                DebuggerMessage message;
                try {
                    message = messageQueue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                if (message instanceof DebuggerMessage.InitializedEvent) {
                    // TODO
                } else if (message instanceof DebuggerMessage.StoppedEvent) {
                    // When debugger suspends by any reason, we no longer need run-to-cursor breakpoint.
                    // - Case when run-to-cursor is reached: of course we don't need it anymore.
                    // - Case when other breakpoint hits before reaching run-to-cursor:
                    //     If we don't clean up, it might be hit later unexpectedly because
                    //     the breakpoint is not shown in anywhere, so need clean up.
                    breakpointHandler.unregisterRunToCursor();
                    StoppedEventArguments stoppedEventArgs = ((StoppedEvent) message).args();

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
                                          xContext = new TLCSuspendContext(remoteProxy);
                                      }
                                      if (xContext instanceof TLCSuspendContext) {
                                          ((TLCSuspendContext) xContext).addExecutionStack(thread, Arrays.asList(stackTraceResponse.getStackFrames()));
                                      }

                                      if (StoppedEventArgumentsReason.EXCEPTION.equals(stoppedEventArgs.getReason())) {
                                          // TODO: Invariant/Error/Unsatisfied
                                          TLCExceptionBreakpointType breakpointType =
                                                  XDebuggerUtil.getInstance().findBreakpointType(
                                                          TLCExceptionBreakpointType.class);
                                          Set<XBreakpoint<TLCExceptionBreakpointProperties>> breakpoints =
                                                  XDebuggerManager.getInstance(session.getProject())
                                                                  .getBreakpointManager()
                                                                  .getDefaultBreakpoints(breakpointType);
                                          XBreakpoint<TLCExceptionBreakpointProperties> breakpoint = breakpoints.stream().findFirst().get();
                                          session.breakpointReached(breakpoint, null, xContext);
                                      } else {
                                          // NOTE: We give up looking-up hit breakpoint (except exception breakpoints above) on stopped event so
                                          // always call positionReached instead of breakpointReached, because
                                          // Some TLCDebugger's breakpoint types (e.g. Spec breakpoint) happen on different line
                                          // from the breakpoint's line, which is hard to look-up without re-implementing
                                          // similar logic with TLCDebugger's halt-condition.
                                          //
                                          // Ideally, TLCDebugger should fill StoppedEventArguments#hitBreakpointIds so
                                          // we can look up hit breakpoint easily.
                                          if (session instanceof XDebugSessionImpl) {
                                              // TLC may send stopped event without breakpoint (e.g. first encounter of evaluation after launch).
                                              // In such case, we want to focus debug tab so passing true to attract arg.
                                              // TODO: Intuitively, this may cause the debugger-tab to be focused even on user-initiated stepping undesirably,
                                              // but seems this just works (i.e. doesn't cause undesirable focus). Why?
                                              ((XDebugSessionImpl) session).positionReached(xContext, true);
                                          }
                                      }
                                  });
                              });
                    });
                } else if (message instanceof DebuggerMessage.TerminatedEvent) {
                } else if (message instanceof DebuggerMessage.OutputEvent) {
                } else if (message instanceof DebuggerMessage.CapabilitiesEvent) {
                    dropFrameHandler.setCanDrop(((CapabilitiesEvent) message).args().getCapabilities().getSupportsStepBack());
                }
            }
        });
        // TODO: Fill mandatory fields
        InitializeRequestArguments initArgs = new InitializeRequestArguments();
        remoteProxy.initialize(initArgs).thenAccept(cap -> {
            dropFrameHandler.setCanDrop(cap.getSupportsStepBack());
            remoteProxy.launch(Map.of());
        });
    }

    // Intellij's debugger feature depends on the process handler returned from here (e.g. XDebugSessionImpl)
    // to manage the lifecycle of the debug process, while default implementation returns dummy-like DefaultDebugProcessHandler.
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
            remoteProxy.next(args);
        }
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        org.eclipse.lsp4j.debug.Thread thread = activeThread(context);
        if (thread != null) {
            StepInArguments args = new StepInArguments();
            args.setThreadId(thread.getId());
            remoteProxy.stepIn(args);
        }
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        org.eclipse.lsp4j.debug.Thread thread = activeThread(context);
        if (thread != null) {
            StepOutArguments args = new StepOutArguments();
            args.setThreadId(thread.getId());
            remoteProxy.stepOut(args);
        }
    }

    @Override
    public @Nullable XDropFrameHandler getDropFrameHandler() {
        return dropFrameHandler;
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        breakpointHandler.registerRunToCursor(position);
        org.eclipse.lsp4j.debug.Thread thread = activeThread(context);
        if (thread != null) {
            ContinueArguments args = new ContinueArguments();
            args.setThreadId(thread.getId());
            remoteProxy.continue_(args);
        }
    }

    @Override
    public void stop() {
        terminated.set(true);
        executorService.shutdown();
//        executorService.awaitTermination()
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
            remoteProxy.continue_(args);
        }
    }

    private static @Nullable org.eclipse.lsp4j.debug.Thread activeThread(XSuspendContext context) {
        if (context instanceof TLCSuspendContext) {
            return ((TLCSuspendContext) context).activeThread();
        }
        return null;
    }
}
