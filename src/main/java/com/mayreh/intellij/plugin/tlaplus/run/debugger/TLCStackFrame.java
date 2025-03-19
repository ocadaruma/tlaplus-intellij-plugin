package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class TLCStackFrame extends XStackFrame {
    @Getter
    @Accessors(fluent = true)
    private final Thread dapThread;
    private final StackFrame dapStackFrame;
    private final IDebugProtocolServer remoteProxy;

    @Override
    public @Nullable XSourcePosition getSourcePosition() {
        return TLCXSourcePosition.createByStackFrame(dapStackFrame);
    }

    @Override
    public void customizePresentation(@NotNull ColoredTextContainer component) {
        component.append(dapStackFrame.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (node.isObsolete()) {
            return;
        }
        ScopesArguments scopesArguments = new ScopesArguments();
        scopesArguments.setFrameId(dapStackFrame.getId());
        remoteProxy.scopes(scopesArguments).thenAcceptAsync(response -> {
            XValueChildrenList children = new XValueChildrenList();
            for (Scope scope : response.getScopes()) {
                children.addBottomGroup(new TLCValueGroup(remoteProxy, scope));
            }
            node.addChildren(children, true);
        }, AppExecutorUtil.getAppExecutorService());
    }
}
