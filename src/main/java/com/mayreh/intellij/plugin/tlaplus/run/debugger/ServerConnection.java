package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import com.intellij.openapi.application.ApplicationManager;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerConnection implements AutoCloseable {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private final CompletableFuture<IDebugProtocolServer> serverFuture;
    private volatile Socket socket;

    public void sendRequest(Consumer<IDebugProtocolServer> command) {
        serverFuture.thenAccept(command);
    }

    public static ServerConnection create(int port, IDebugProtocolClient client) {
        CompletableFuture<IDebugProtocolServer> proxyFuture = new CompletableFuture<>();
        ServerConnection conn = new ServerConnection(proxyFuture);
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Socket socket;
            try {
                socket = connect(port);
                conn.socket = socket;
                Launcher<IDebugProtocolServer> launcher = DSPLauncher.createClientLauncher(
                        client, socket.getInputStream(), socket.getOutputStream());
                launcher.startListening();
                proxyFuture.complete(launcher.getRemoteProxy());
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                proxyFuture.completeExceptionally(e);
            }
        });
        return conn;
    }

    private static Socket connect(int port) throws IOException, InterruptedException {
        long t0 = System.nanoTime();
        IOException lastException = null;
        while (System.nanoTime() - t0 < CONNECT_TIMEOUT.toNanos()) {
            try {
                return new Socket("localhost", port);
            } catch (IOException e) {
                lastException = e;
                Thread.sleep(10);
            }
        }
        throw new IOException("Failed to connect to debugger", lastException);
    }

    @Override
    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
