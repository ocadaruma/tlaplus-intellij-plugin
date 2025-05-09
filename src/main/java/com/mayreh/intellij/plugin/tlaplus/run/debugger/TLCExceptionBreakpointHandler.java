package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ExceptionBreakpointsFilter;
import org.eclipse.lsp4j.debug.ExceptionFilterOptions;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsArguments;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;

public class TLCExceptionBreakpointHandler extends XBreakpointHandler<XBreakpoint<TLCExceptionBreakpointProperties>> {
    private final XDebugSession debugSession;
    private final ServerConnection serverConnection;
    private final Set<ExceptionFilterOptions> exceptionFilters;

    public TLCExceptionBreakpointHandler(XDebugSession debugSession, ServerConnection serverConnection) {
        super(TLCExceptionBreakpointType.class);
        this.debugSession = debugSession;
        this.serverConnection = serverConnection;
        exceptionFilters = ContainerUtil.newConcurrentSet();
    }

    @Override
    public void registerBreakpoint(@NotNull XBreakpoint<TLCExceptionBreakpointProperties> breakpoint) {
        if (breakpoint.getProperties().getExceptionFilterId() != null) {
            ExceptionFilterOptions options = new ExceptionFilterOptions();
            options.setFilterId(breakpoint.getProperties().getExceptionFilterId());
            exceptionFilters.add(options);
            updateRemoteBreakpoints();
        }
    }

    @Override
    public void unregisterBreakpoint(@NotNull XBreakpoint<TLCExceptionBreakpointProperties> breakpoint,
                                     boolean temporary) {
        if (breakpoint.getProperties().getExceptionFilterId() != null) {
            ExceptionFilterOptions options = new ExceptionFilterOptions();
            options.setFilterId(breakpoint.getProperties().getExceptionFilterId());
            exceptionFilters.remove(options);
            updateRemoteBreakpoints();
        }
    }

    public void createOrRemoveBreakpoints(Capabilities capabilities) {
        if (capabilities.getExceptionBreakpointFilters() == null) {
            return;
        }
        XBreakpointManager manager =
                XDebuggerManager.getInstance(debugSession.getProject()).getBreakpointManager();
        TLCExceptionBreakpointType breakpointType =
                XDebuggerUtil.getInstance().findBreakpointType(TLCExceptionBreakpointType.class);

        Collection<? extends XBreakpoint<TLCExceptionBreakpointProperties>> breakpoints =
                ApplicationManager.getApplication().runReadAction(
                        (Computable<? extends Collection<? extends XBreakpoint<TLCExceptionBreakpointProperties>>>)
                                () -> manager.getBreakpoints(breakpointType));
        // create necessary breakpoints
        for (ExceptionBreakpointsFilter dapFilter : capabilities.getExceptionBreakpointFilters()) {
            if (breakpoints.stream().anyMatch(
                    b ->
                            b.getProperties() != null &&
                            dapFilter.getFilter().equals(b.getProperties().getExceptionFilterId()))) {
                continue;
            }
            TLCExceptionBreakpointProperties props = new TLCExceptionBreakpointProperties();
            props.setExceptionFilterId(dapFilter.getFilter());
            props.setExceptionFilterLabel(dapFilter.getLabel());
            props.setByFilter(dapFilter);
            ApplicationManager.getApplication().invokeAndWait(() -> {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    manager.addBreakpoint(
                            breakpointType,
                            props);
                });
            });
        }

        // Remove unnecessary breakpoints
        for (XBreakpoint<TLCExceptionBreakpointProperties> breakpoint : breakpoints) {
            if (breakpoint.getProperties() == null ||
                breakpoint.getProperties().getExceptionFilterId() == null) {
                continue;
            }
            if (Arrays.stream(capabilities.getExceptionBreakpointFilters()).noneMatch(
                    f -> f.getFilter().equals(breakpoint.getProperties().getExceptionFilterId()))) {
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        manager.removeBreakpoint(breakpoint);
                    });
                });
            }
        }
    }

    public Optional<XBreakpoint<TLCExceptionBreakpointProperties>> findBreakpointByFilterId(@NotNull String filterId) {
        XBreakpointManager manager = XDebuggerManager.getInstance(debugSession.getProject()).getBreakpointManager();
        TLCExceptionBreakpointType breakpointType = XDebuggerUtil.getInstance().findBreakpointType(TLCExceptionBreakpointType.class);
        return ApplicationManager.getApplication().runReadAction(
                (Computable<Optional<XBreakpoint<TLCExceptionBreakpointProperties>>>) () -> {
                    Collection<? extends XBreakpoint<TLCExceptionBreakpointProperties>> breakpoints = manager.getBreakpoints(breakpointType);
                    return breakpoints.stream()
                                      .filter(b -> b.getProperties() != null &&
                                                   filterId.equals(b.getProperties().getExceptionFilterId()))
                                      .map(b -> (XBreakpoint<TLCExceptionBreakpointProperties>) b)
                                      .findFirst();
                });
    }

    private void updateRemoteBreakpoints() {
        SetExceptionBreakpointsArguments args = new SetExceptionBreakpointsArguments();
        ExceptionFilterOptions[] options = exceptionFilters.toArray(new ExceptionFilterOptions[0]);
        String[] ids = Arrays.stream(options)
                             .map(ExceptionFilterOptions::getFilterId)
                             .toArray(String[]::new);
        args.setFilters(ids);
        args.setFilterOptions(options);
        serverConnection.sendRequest(remoteProxy -> {
            remoteProxy.setExceptionBreakpoints(args);
        });
    }
}
