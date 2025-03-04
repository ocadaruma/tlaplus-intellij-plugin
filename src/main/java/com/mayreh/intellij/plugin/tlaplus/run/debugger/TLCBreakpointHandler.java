package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

public class TLCBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<TLCBreakpointProperties>> {
    public TLCBreakpointHandler() {
        super(TLCBreakpointType.class);
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<TLCBreakpointProperties> breakpoint) {
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<TLCBreakpointProperties> breakpoint,
                                     boolean temporary) {
    }
}
