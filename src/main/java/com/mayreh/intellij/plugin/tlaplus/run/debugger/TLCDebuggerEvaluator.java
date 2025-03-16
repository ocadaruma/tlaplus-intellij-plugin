package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;

public class TLCDebuggerEvaluator extends XDebuggerEvaluator {
    @Override
    public void evaluate(@NotNull String expression,
                         @NotNull XEvaluationCallback callback,
                         @Nullable XSourcePosition expressionPosition) {

    }
}
