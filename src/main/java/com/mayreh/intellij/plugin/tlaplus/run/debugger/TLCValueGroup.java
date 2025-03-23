package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.jetbrains.annotations.NotNull;

import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueGroup;

public class TLCValueGroup extends XValueGroup {
    private final ServerConnection serverConnection;
    private final Scope scope;

    public TLCValueGroup(ServerConnection serverConnection, Scope scope) {
        super(scope.getName());
        this.serverConnection = serverConnection;
        this.scope = scope;
    }

    @Override
    public boolean isRestoreExpansion() {
        // FIXME: Even with this, expanded nodes don't persist when stepping through
        return true;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (node.isObsolete()) {
            return;
        }
        if (scope.getVariablesReference() <= 0) {
            return;
        }
        VariablesArguments args = new VariablesArguments();
        args.setVariablesReference(scope.getVariablesReference());
        serverConnection.sendRequest(remoteProxy -> {
            remoteProxy.variables(args).thenAccept(response -> {
                XValueChildrenList children = new XValueChildrenList();
                for (Variable variable : response.getVariables()) {
                    children.add(variable.getName(), new TLCDebuggerValue(remoteProxy, variable));
                }
                node.addChildren(children, true);
            });
        });
    }
}
