package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import javax.swing.tree.DefaultMutableTreeNode;

import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;

class TraceVariableNode extends DefaultMutableTreeNode {
    public final String key;
    public final TraceVariableValueWrapper value;

    TraceVariableNode(String key, TraceVariableValue value, ValueDiffType diffType) {
        super(key);
        this.key = key;
        this.value = new TraceVariableValueWrapper(diffType, value);
    }
}
