package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.eclipse.lsp4j.debug.ExceptionBreakpointsFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class TLCExceptionBreakpointProperties extends XBreakpointProperties<TLCExceptionBreakpointProperties> {
    /**
     * Excerpt from {@link ExceptionBreakpointsFilter}.
     * Since properties will be stored/loaded persistently, we don't want to use DAP's ExceptionBreakpointsFilter directly
     * which may cause unexpected problems when upgrading LSP4J library.
     */
    @Data
    public static class ExceptionFilter {
        private String filterId;
        private String label;
    }

    @Getter
    @Setter
    private ExceptionFilter filter;

    @Override
    public @Nullable TLCExceptionBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TLCExceptionBreakpointProperties state) {
        filter = state.filter;
    }
}
