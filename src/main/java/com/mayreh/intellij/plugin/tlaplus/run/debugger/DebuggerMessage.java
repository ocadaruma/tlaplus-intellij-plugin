package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.CapabilitiesEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;

/**
 * Represents a message (either of event or request) sent from the TLC Debugger to the client.
 * <p>
 * For simplicity, we define only subset of messages in the protocol that TLC debugger may send as of 2025-03-03.
 */
public sealed interface DebuggerMessage
        permits DebuggerMessage.InitializedEvent,
                DebuggerMessage.StoppedEvent,
                DebuggerMessage.TerminatedEvent,
                DebuggerMessage.OutputEvent,
                DebuggerMessage.CapabilitiesEvent
{
    record InitializedEvent() implements DebuggerMessage {}
    record StoppedEvent(StoppedEventArguments args) implements DebuggerMessage {}
    record TerminatedEvent(TerminatedEventArguments args) implements DebuggerMessage {}
    record OutputEvent(OutputEventArguments args) implements DebuggerMessage {}
    record CapabilitiesEvent(CapabilitiesEventArguments args) implements DebuggerMessage {}
}
