package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

public class TLCExecutionStack extends XExecutionStack {
    private final IDebugProtocolServer remoteProxy;
    private final Thread dapThread;
    private final CompletableFuture<List<TLCStackFrame>> framesFuture;
    private final AtomicBoolean initialized;

    public TLCExecutionStack(IDebugProtocolServer remoteProxy, Thread dapThread) {
        super(getThreadName(dapThread));
        this.remoteProxy = remoteProxy;
        this.dapThread = dapThread;
        framesFuture = new CompletableFuture<>();
        initialized = new AtomicBoolean(false);
    }

    @Override
    public @Nullable XStackFrame getTopFrame() {
        maybeInitialize();
        // FIXME: This join() causes deadlock due to positionReached()
        return ContainerUtil.getFirstItem(framesFuture.join());
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        if (container.isObsolete()) {
            return;
        }
        maybeInitialize();
        framesFuture.thenAccept(frames -> {
            container.addStackFrames(frames, true);
        });
    }

    private void maybeInitialize() {
        if (initialized.compareAndSet(false, true)) {
            StackTraceArguments args = new StackTraceArguments();
            args.setThreadId(dapThread.getId());
            remoteProxy.stackTrace(args).thenAcceptAsync(response -> {
                List<TLCStackFrame> frames = new ArrayList<>();
                for (StackFrame frame : response.getStackFrames()) {
                    frames.add(new TLCStackFrame(dapThread, frame, remoteProxy));
                }
                framesFuture.complete(frames);
            }, AppExecutorUtil.getAppExecutorService());
        }
    }

    private static String getThreadName(Thread thread) {
        String name = thread.getName();
        return !StringUtil.isEmpty(name) ? name : "Thread #" + thread.getId();
    }
}
