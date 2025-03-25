package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.ExceptionBreakpointsFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TLCExceptionBreakpointProperties extends XBreakpointProperties<TLCExceptionBreakpointProperties> {
    /**
     * The ID of the exception filter defined by {@link ExceptionBreakpointsFilter}.
     */
    private String exceptionFilterId;
    /**
     * The label of the exception filter defined by {@link ExceptionBreakpointsFilter}.
     */
    private String exceptionFilterLabel;

    @Override
    public @Nullable TLCExceptionBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TLCExceptionBreakpointProperties state) {
        exceptionFilterId = state.exceptionFilterId;
        exceptionFilterLabel = state.exceptionFilterLabel;
    }



    public void setByFilter(ExceptionBreakpointsFilter filter) {
        exceptionFilterId = filter.getFilter();
        exceptionFilterLabel = filter.getLabel();
    }
}
