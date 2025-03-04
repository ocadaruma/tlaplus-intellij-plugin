package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

/**
 * Responsible for persisting additional breakpoint properties, such as
 * JavaBreakpointProperties's class filters, instance filters, etc.
 * <p>
 * We don't support any additional properties for now for TLC debugger, so
 * this class is almost no-op.
 */
public class TLCBreakpointProperties extends XBreakpointProperties<TLCBreakpointProperties> {
    @Override
    public @Nullable TLCBreakpointProperties getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull TLCBreakpointProperties state) {
        // noop
    }
}
