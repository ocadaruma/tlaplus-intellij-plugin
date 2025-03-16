package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCStackFrame extends XStackFrame {
    private final VirtualFile file;
    private final PsiElement element;
    private final StackFrame dapStackFrame;
    private final IDebugProtocolServer remoteProxy;

    @Override
    public @Nullable XSourcePosition getSourcePosition() {
        return new TLCXSourcePosition(file, element);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (node.isObsolete()) {
            return;
        }
        ScopesArguments scopesArguments = new ScopesArguments();
        scopesArguments.setFrameId(dapStackFrame.getId());
        remoteProxy.scopes(scopesArguments).thenAccept(response -> {
            XValueChildrenList children = new XValueChildrenList();
            for (Scope scope : response.getScopes()) {
                children.addBottomGroup(new TLCValueGroup(remoteProxy, scope));
            }
            node.addChildren(children, true);
        });
    }
}
