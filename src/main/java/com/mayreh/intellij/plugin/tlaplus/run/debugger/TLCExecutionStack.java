package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.List;

import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

import lombok.Getter;

public class TLCExecutionStack extends XExecutionStack {
    @Getter
    private final Thread dapThread;
    private final List<TLCStackFrame> stackFrames;

    public TLCExecutionStack(IDebugProtocolServer remoteProxy, Thread dapThread, List<StackFrame> stackFrames) {
        super(getThreadName(dapThread));
        this.dapThread = dapThread;
        this.stackFrames = stackFrames.stream()
                                      .map(f -> new TLCStackFrame(dapThread, f, remoteProxy))
                                      .toList();
    }

    @Override
    public @Nullable XStackFrame getTopFrame() {
        return ContainerUtil.getFirstItem(stackFrames);
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        if (container.isObsolete()) {
            return;
        }
        container.addStackFrames(stackFrames, true);
    }

    private static String getThreadName(Thread thread) {
        String name = thread.getName();
        return !StringUtil.isEmpty(name) ? name : "Thread #" + thread.getId();
    }
}
