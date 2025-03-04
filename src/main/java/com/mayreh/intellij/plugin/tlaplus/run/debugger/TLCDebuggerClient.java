package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.concurrent.BlockingQueue;

import org.eclipse.lsp4j.debug.CapabilitiesEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCDebuggerClient implements IDebugProtocolClient {
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
