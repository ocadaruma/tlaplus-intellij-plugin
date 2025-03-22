package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCSuspendContext extends XSuspendContext {
    private final IDebugProtocolServer remoteProxy;
    private final List<TLCExecutionStack> stacks = ContainerUtil.createConcurrentList();

    @Override
    public @Nullable XExecutionStack getActiveExecutionStack() {
        return ContainerUtil.getLastItem(stacks);
    }

    public @Nullable Thread activeThread() {
        return Optional.ofNullable(ContainerUtil.getLastItem(stacks))
                       .map(TLCExecutionStack::getDapThread)
                       .orElse(null);
    }

    public void addExecutionStack(@NotNull Thread dapThread, @NotNull List<StackFrame> stackFrames) {
        stacks.add(new TLCExecutionStack(remoteProxy, dapThread, stackFrames));
    }
}
