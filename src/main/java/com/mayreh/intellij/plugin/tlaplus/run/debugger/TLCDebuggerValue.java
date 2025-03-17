package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;

import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;

public class TLCDebuggerValue extends XNamedValue {
    private final IDebugProtocolServer remoteProxy;
    private final Variable variable;

    public TLCDebuggerValue(IDebugProtocolServer remoteProxy, Variable variable) {
        super(variable.getName());
        this.remoteProxy = remoteProxy;
        this.variable = variable;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        node.setPresentation(null, null, variable.getValue(), variable.getVariablesReference() > 0);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (node.isObsolete()) {
            return;
        }
        if (variable.getVariablesReference() <= 0) {
            return;
        }
        VariablesArguments args = new VariablesArguments();
        args.setVariablesReference(variable.getVariablesReference());
        remoteProxy.variables(args).thenAcceptAsync(response -> {
            XValueChildrenList children = new XValueChildrenList();
            for (Variable variable : response.getVariables()) {
                children.add(variable.getName(), new TLCDebuggerValue(remoteProxy, variable));
            }
            node.addChildren(children, true);
        }, AppExecutorUtil.getAppExecutorService());
    }
}
