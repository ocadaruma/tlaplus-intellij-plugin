package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.xmlb.annotations.Transient;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

import lombok.Getter;
import lombok.Setter;

// necessary to add accessors to allow serialization
@Getter
@Setter
public class TLCBreakpointProperties extends XBreakpointProperties<TLCBreakpointProperties> {
    private Integer startOffset;
    private Integer endOffset;

    @Override
    public @Nullable TLCBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TLCBreakpointProperties state) {
        startOffset = state.startOffset;
        endOffset = state.endOffset;
    }

    // need transient, otherwise Intellij will try to ser/de this field
    // and it fails because TextRange doesn't have a no-arg constructor
    @Transient
    public @Nullable TextRange getTextRange() {
        if (startOffset == null || endOffset == null) {
            return null;
        }
        return new TextRange(startOffset, endOffset);
    }

    public void setTextRange(TextRange textRange) {
        startOffset = textRange.getStartOffset();
        endOffset = textRange.getEndOffset();
    }
}
