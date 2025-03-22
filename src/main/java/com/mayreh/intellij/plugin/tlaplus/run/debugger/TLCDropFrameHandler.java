package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.StepBackArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;

import com.intellij.xdebugger.frame.XDropFrameHandler;
import com.intellij.xdebugger.frame.XStackFrame;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class TLCDropFrameHandler implements XDropFrameHandler {
    private final IDebugProtocolServer remoteProxy;

    @Getter
    @Setter
    private volatile boolean canDrop;

    @Override
    public boolean canDrop(@NotNull XStackFrame frame) {
        return canDrop;
    }

    @Override
    public void drop(@NotNull XStackFrame frame) {
        if (frame instanceof TLCStackFrame) {
            int threadId = ((TLCStackFrame) frame).dapThread().getId();
            StepBackArguments args = new StepBackArguments();
            args.setThreadId(threadId);
            remoteProxy.stepBack(args);
        }
    }
}
