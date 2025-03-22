package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointType;

public class TLCExceptionBreakpointType extends XBreakpointType<XBreakpoint<TLCExceptionBreakpointProperties>, TLCExceptionBreakpointProperties> {
    public TLCExceptionBreakpointType() {
        super("TLCExceptionBreakpointType", "TLC Exceptions");
    }

    @Override
    public @Nls String getDisplayText(XBreakpoint<TLCExceptionBreakpointProperties> breakpoint) {
        return "TLC Exception Breakpoint";
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

    @Override
    public @Nullable XBreakpoint<TLCExceptionBreakpointProperties> createDefaultBreakpoint(
            @NotNull XBreakpointCreator<TLCExceptionBreakpointProperties> creator) {
        return creator.createBreakpoint(new TLCExceptionBreakpointProperties());
    }
}
