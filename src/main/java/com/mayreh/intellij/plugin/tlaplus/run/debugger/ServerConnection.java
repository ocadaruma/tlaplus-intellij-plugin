package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Responsible for creating and provide a debugger server connection asynchronously.
 * <p>Why async?:
 * {@link TLCDebugProcess} requires a connection for communicating with debugger server, which should be
 * passed through the constructor args.
 * However, {@link TLCDebugProcess} instantiation must be done immediately without waiting connection to be
 * established to show debug console quickly for UX reason.
 * Hence, the connection is established and provided asynchronously by this class.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerConnection implements AutoCloseable {
    private static final Logger log = Logger.getInstance(ServerConnection.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private final CompletableFuture<IDebugProtocolServer> serverFuture;
    private volatile Socket socket;
    private volatile boolean closed;

    public void sendRequest(Consumer<IDebugProtocolServer> command) {
        serverFuture.thenAccept(command);
    }

    public static ServerConnection create(int port, IDebugProtocolClient client) {
        CompletableFuture<IDebugProtocolServer> serverFuture = new CompletableFuture<>();
        ServerConnection conn = new ServerConnection(serverFuture);
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Socket socket;
            try {
                socket = connect(port);
                // ServerConnection#close might be called before setting socket to conn after connecting.
                // To avoid leaking socket in this case, we should check and close socket if necessary.
                synchronized (conn) {
                    if (conn.closed) {
                        try {
                            socket.close();
                        } finally {
                            conn.serverFuture.completeExceptionally(new IOException("Connection closed"));
                        }
                        return;
                    }
                    conn.socket = socket;
                }
                Launcher<IDebugProtocolServer> launcher = DSPLauncher.createClientLauncher(
                        client, socket.getInputStream(), socket.getOutputStream());
                launcher.startListening();
                serverFuture.complete(launcher.getRemoteProxy());
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                serverFuture.completeExceptionally(e);
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
    public synchronized void close() {
        closed = true;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                log.warn("Failed to close socket", e);
            }
        }
    }
}
