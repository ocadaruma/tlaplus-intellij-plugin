package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.concurrent.BlockingQueue;

import org.eclipse.lsp4j.debug.CapabilitiesEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;

import lombok.RequiredArgsConstructor;

/**
 * {@link IDebugProtocolClient} implementation which just puts received messages into a queue.
 * <p>Why not handling messages (e.g. suspends the session on stopped event,... etc) directly in this class?
 * To handle messages, we need {@link IDebugProtocolServer} to request to debugger server.
 * However, {@link DSPLauncher} requires {@link IDebugProtocolClient} before instantiating {@link IDebugProtocolServer},
 * which causes a kind of circular dependency.
 * Another possible solution is to set {@link IDebugProtocolServer} after creating {@link IDebugProtocolClient},
 * but it introduces mutability and null-check all over the place, so we decided to decouple the dependency by using a queue instead.
 */
@RequiredArgsConstructor
public class DebugProtocolReceiver implements IDebugProtocolClient {
    private final BlockingQueue<DebuggerMessage> messageQueue;

    @Override
    public void initialized() {
        messageQueue.add(new DebuggerMessage.InitializedEvent());
    }

    @Override
    public void stopped(StoppedEventArguments args) {
        messageQueue.add(new DebuggerMessage.StoppedEvent(args));
    }

    @Override
    public void terminated(TerminatedEventArguments args) {
        messageQueue.add(new DebuggerMessage.TerminatedEvent(args));
    }

    @Override
    public void output(OutputEventArguments args) {
        messageQueue.add(new DebuggerMessage.OutputEvent(args));
    }

    @Override
    public void capabilities(CapabilitiesEventArguments args) {
        messageQueue.add(new DebuggerMessage.CapabilitiesEvent(args));
    }
}
