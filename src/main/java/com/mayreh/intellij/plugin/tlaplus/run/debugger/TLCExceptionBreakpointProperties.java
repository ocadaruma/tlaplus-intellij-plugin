package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

public class TLCExceptionBreakpointProperties extends XBreakpointProperties<TLCExceptionBreakpointProperties> {
    @Override
    public @Nullable TLCExceptionBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TLCExceptionBreakpointProperties state) {
        // noop
    }
}
