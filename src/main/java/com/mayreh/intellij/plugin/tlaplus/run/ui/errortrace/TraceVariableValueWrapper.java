package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
class TraceVariableValueWrapper {
    ValueDiffType diffType;
    TraceVariableValue value;

    // Should return TraceVariableValue#asString so that it will be rendered as
    // cell text and clipboard content
    @Override
    public String toString() {
        return value.asString();
    }
}
