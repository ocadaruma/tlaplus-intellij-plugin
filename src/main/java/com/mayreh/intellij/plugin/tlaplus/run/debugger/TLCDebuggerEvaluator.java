package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateArgumentsContext;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCDebuggerEvaluator extends XDebuggerEvaluator {
    private final ServerConnection serverConnection;
    private final StackFrame dapStackFrame;

    @Override
    public void evaluate(@NotNull String expression,
                         @NotNull XEvaluationCallback callback,
                         @Nullable XSourcePosition expressionPosition) {
        EvaluateArguments args = new EvaluateArguments();
        args.setExpression(expression);
        args.setFrameId(dapStackFrame.getId());
        args.setContext(EvaluateArgumentsContext.WATCH);
        serverConnection.sendRequest(remoteProxy -> {
            remoteProxy.evaluate(args).thenAccept(response -> {
                Variable variable = new Variable();
                variable.setName(expression);
                variable.setValue(response.getResult());
                variable.setVariablesReference(response.getVariablesReference());
                variable.setNamedVariables(response.getNamedVariables());
                variable.setIndexedVariables(response.getIndexedVariables());
                callback.evaluated(new TLCDebuggerValue(remoteProxy, variable));
            });
        });
    }
}
