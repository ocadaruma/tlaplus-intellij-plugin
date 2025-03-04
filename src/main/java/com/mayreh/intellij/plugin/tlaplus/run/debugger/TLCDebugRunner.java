package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.io.IOException;

import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
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
        RunContentDescriptor descriptor = XDebuggerManager
                .getInstance(executionEnvironment.getProject())
                .startSession(executionEnvironment, new XDebugProcessStarter() {
                    @Override
                    public @NotNull XDebugProcess start(@NotNull XDebugSession session)
                            throws ExecutionException {
                        return new TLCDebugProcess(session, executionResult);
                    }
                }).getRunContentDescriptor();
        promise.setResult(descriptor);
        return promise;
    }
}
