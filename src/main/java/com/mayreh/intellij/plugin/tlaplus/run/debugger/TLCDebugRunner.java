package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.mayreh.intellij.plugin.tlaplus.run.TLCRunConfiguration;

@SuppressWarnings("rawtypes")
public class TLCDebugRunner extends AsyncProgramRunner {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

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
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Socket socket;
            try {
                socket = connect(port); // FIXME: Better not to block here to open the console quickly
            } catch (IOException e) {
                promise.setError(e);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                promise.setError(e);
                return;
            }

            ApplicationManager.getApplication().invokeLater(() -> {
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
                                    try {
                                        Launcher<IDebugProtocolServer> launcher = DSPLauncher.createClientLauncher(
                                                receiver, socket.getInputStream(), socket.getOutputStream());
                                        launcher.startListening();
                                        TLCDebugProcess process = new TLCDebugProcess(
                                                session, messageQueue, launcher.getRemoteProxy(), executionResult);
                                        return process;
                                    } catch (IOException e) {
                                        throw new ExecutionException(e);
                                    }
                                }
                            }).getRunContentDescriptor();
                } catch (ExecutionException e) {
                    promise.setError(e);
                    return;
                }
                promise.setResult(descriptor);
            });
        });
        return promise;
    }

    private static Socket connect(int port) throws IOException, InterruptedException {
        long t0 = System.nanoTime();
        while (System.nanoTime() - t0 < CONNECT_TIMEOUT.toNanos()) {
            try {
                return new Socket("localhost", port);
            } catch (IOException e) {
                Thread.sleep(10);
            }
        }
        throw new IOException("Failed to connect to debugger");
    }
}
