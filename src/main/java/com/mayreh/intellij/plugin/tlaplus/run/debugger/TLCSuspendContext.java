package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCSuspendContext extends XSuspendContext {
    private final IDebugProtocolServer remoteProxy;
    private volatile Thread dapThread;

    @Override
    public @Nullable XExecutionStack getActiveExecutionStack() {
        Thread thread = dapThread;
        if (thread == null) {
            return null;
        }
        return new TLCExecutionStack(remoteProxy, thread);
    }

    public @Nullable Thread activeThread() {
        return dapThread;
    }

    public void activateThread(@NotNull Thread dapThread) {
        this.dapThread = dapThread;
    }
}
