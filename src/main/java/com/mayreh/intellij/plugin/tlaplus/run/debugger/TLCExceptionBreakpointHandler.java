package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;

import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;

public class TLCExceptionBreakpointHandler extends XBreakpointHandler<XBreakpoint<TLCExceptionBreakpointProperties>> {
    private final XDebugSession debugSession;
    private final ServerConnection serverConnection;

    public TLCExceptionBreakpointHandler(XDebugSession debugSession, ServerConnection serverConnection) {
        super(TLCExceptionBreakpointType.class);
        this.debugSession = debugSession;
        this.serverConnection = serverConnection;
    }

    @Override
    public void registerBreakpoint(@NotNull XBreakpoint<TLCExceptionBreakpointProperties> breakpoint) {
        // TODO: implement
    }

    @Override
    public void unregisterBreakpoint(@NotNull XBreakpoint<TLCExceptionBreakpointProperties> breakpoint,
                                     boolean temporary) {
        // TODO: implement
    }
}
