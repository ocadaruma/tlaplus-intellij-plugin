package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.mayreh.intellij.plugin.tlaplus.run.TLCRunConfiguration;

@SuppressWarnings("rawtypes")
public class TLCDebugRunner extends AsyncProgramRunner {
    @Override
    public @NotNull @NonNls String getRunnerId() {
        return "TLCDebugRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof TLCRunConfiguration;
    }

    @Override
    protected @NotNull Promise<RunContentDescriptor> execute(
            @NotNull ExecutionEnvironment executionEnvironment,
            @NotNull RunProfileState runProfileState)
            throws ExecutionException {
        AsyncPromise<RunContentDescriptor> promise = new AsyncPromise<>();
        int port;
        try {
            port = NetUtils.findAvailableSocketPort();
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
        executionEnvironment.putUserData(TLCRunConfiguration.DEBUGGER_PORT, port);
        ExecutionResult executionResult = runProfileState.execute(executionEnvironment.getExecutor(), this);
        ApplicationManager.getApplication().invokeLater(() -> {
            // Save right before starting debug process.
            // Otherwise, stackframe source location might be calculated based on last saved content,
            // not the current content and causes wrong position highlighting.
            FileDocumentManager.getInstance().saveAllDocuments();

            RunContentDescriptor descriptor;
            try {
                descriptor = XDebuggerManager
                        .getInstance(executionEnvironment.getProject())
                        .startSession(executionEnvironment, new XDebugProcessStarter() {
                            @Override
                            public @NotNull XDebugProcess start(@NotNull XDebugSession session)
                                    throws ExecutionException {
                                BlockingQueue<DebuggerMessage> messageQueue = new LinkedBlockingQueue<>();
                                DebugProtocolReceiver receiver = new DebugProtocolReceiver(messageQueue);

                                // The resources we must ensure to release are below 2:
                                // 1. TLC process
                                //   - For this, once TLCDebugProcess is created and debug session is started,
                                //     we can rely on user's explicit termination (by stop/restart button or IDE quit)
                                //     So what we need to care is until TLCDebugProcess is created.
                                //     As described below, there's no place which might fail until TLCDebugProcess instantiation
                                //     so this case is ok.
                                //     NOTE: If IDE quits forcibly, process might leak, but we can't do anything for this.
                                // 2. DAP server connection
                                //   - Similarly, there's no place which might fail after ServerConnection instantiation.
                                //     TLCDebugProcess#close will close the connection, so we can rely on user's explicit termination
                                //     which will call TLCDebugProcess#close.
                                //
                                // Connection establishment (which might fail) is done asynchronously,
                                // which means below 2 lines are expected never to throw.
                                // Hence, we don't need to try-catch for ensuring process destruction on failure.
                                ServerConnection serverConnection = ServerConnection.create(port, receiver);
                                return new TLCDebugProcess(session, messageQueue, serverConnection, executionResult);
                            }
                        }).getRunContentDescriptor();
            } catch (Exception e) {
                promise.setError(e);
                return;
            }
            promise.setResult(descriptor);
        });
        return promise;
    }
}
