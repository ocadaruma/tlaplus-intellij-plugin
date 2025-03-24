package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointType;

public class TLCExceptionBreakpointType extends XBreakpointType<XBreakpoint<TLCExceptionBreakpointProperties>, TLCExceptionBreakpointProperties> {
    public TLCExceptionBreakpointType() {
        super("TLCExceptionBreakpointType", "TLC Exceptions");
    }

    @Override
    public @Nls String getDisplayText(XBreakpoint<TLCExceptionBreakpointProperties> breakpoint) {
        if (breakpoint.getProperties() != null && breakpoint.getProperties().getFilter() != null) {
            return breakpoint.getProperties().getFilter().getLabel();
        }
        return "TLC Exception";
    }

    @Override
    public @NotNull Icon getEnabledIcon() {
        return AllIcons.Debugger.Db_exception_breakpoint;
    }

    @Override
    public @NotNull Icon getDisabledIcon() {
        return AllIcons.Debugger.Db_disabled_exception_breakpoint;
    }

    @Override
    public @NotNull Icon getMutedEnabledIcon() {
        return AllIcons.Debugger.Db_exception_breakpoint;
    }

    @Override
    public @NotNull Icon getMutedDisabledIcon() {
        return AllIcons.Debugger.Db_exception_breakpoint;
    }
}
