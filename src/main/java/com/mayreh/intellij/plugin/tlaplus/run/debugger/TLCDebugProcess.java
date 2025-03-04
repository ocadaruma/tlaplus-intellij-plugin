package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

import javax.swing.event.HyperlinkListener;

import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XDropFrameHandler;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValueMarkerProvider;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import com.intellij.xdebugger.ui.XDebugTabLayouter;

public class TLCDebugProcess extends XDebugProcess {
    private static final Logger log = Logger.getInstance(TLCDebugProcess.class);

    private final ExecutionResult executionResult;
//    private final IDebugProtocolServer remoteProxy;
//    private final BlockingQueue<DebuggerMessage> messageQueue;

    public TLCDebugProcess(@NotNull XDebugSession session, ExecutionResult executionResult) {
        super(session);
        this.executionResult = executionResult;

//        Executors.newSingleThreadExecutor().execute(() -> {
//            while (true) {
//                DebuggerMessage message = messageQueue.poll();
//                if (message instanceof DebuggerMessage.InitializedEvent) {
//                    remoteProxy.initialized();
//                } else if (message instanceof DebuggerMessage.StoppedEvent) {
//                    remoteProxy.stopped(((DebuggerMessage.StoppedEvent) message).getArgs());
//                } else if (message instanceof DebuggerMessage.TerminatedEvent) {
//                    remoteProxy.terminated(((DebuggerMessage.TerminatedEvent) message).getArgs());
//                    break;
//                } else if (message instanceof DebuggerMessage.OutputEvent) {
//
//                } else if (message instanceof DebuggerMessage.CapabilitiesEvent) {
//                    remoteProxy.capabilities(((DebuggerMessage.CapabilitiesEvent) message).getArgs());
//                }
//            }
//        });
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
        return super.getBreakpointHandlers();
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
    }

    @Override
    public void startPausing() {
        super.startPausing();
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        super.startStepOver(context);
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {
        super.startForceStepInto(context);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        super.startStepInto(context);
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        super.startStepOut(context);
    }

    @Override
    public @Nullable XSmartStepIntoHandler<?> getSmartStepIntoHandler() {
        return super.getSmartStepIntoHandler();
    }

    @Override
    public @Nullable XDropFrameHandler getDropFrameHandler() {
        return super.getDropFrameHandler();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public @NotNull Promise<Object> stopAsync() {
        return super.stopAsync();
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        super.resume(context);
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        super.runToPosition(position, context);
    }

    @Override
    public boolean checkCanPerformCommands() {
        return super.checkCanPerformCommands();
    }

    @Override
    public boolean checkCanInitBreakpoints() {
        return super.checkCanInitBreakpoints();
    }

    @Override
    public @Nullable XValueMarkerProvider<?, ?> createValueMarkerProvider() {
        return super.createValueMarkerProvider();
    }

    @Override
    public void registerAdditionalActions(@NotNull DefaultActionGroup leftToolbar,
                                          @NotNull DefaultActionGroup topToolbar,
                                          @NotNull DefaultActionGroup settings) {
        super.registerAdditionalActions(leftToolbar, topToolbar, settings);
    }

    @Override
    public @Nls String getCurrentStateMessage() {
        return super.getCurrentStateMessage();
    }

    @Override
    public @Nullable HyperlinkListener getCurrentStateHyperlinkListener() {
        return super.getCurrentStateHyperlinkListener();
    }

    @Override
    public @NotNull XDebugTabLayouter createTabLayouter() {
        return super.createTabLayouter();
    }

    @Override
    public boolean isValuesCustomSorted() {
        return super.isValuesCustomSorted();
    }

    @Override
    public @Nullable XDebuggerEvaluator getEvaluator() {
        return super.getEvaluator();
    }

    @Override
    public boolean isLibraryFrameFilterSupported() {
        return super.isLibraryFrameFilterSupported();
    }

    @Override
    public void logStack(@NotNull XSuspendContext suspendContext, @NotNull XDebugSession session) {
        super.logStack(suspendContext, session);
    }

    @Override
    public boolean dependsOnPlugin(@NotNull IdeaPluginDescriptor descriptor) {
        return super.dependsOnPlugin(descriptor);
    }
}
