package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

import lombok.Value;

public class TLCBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<TLCBreakpointProperties>> {
    private final XDebugSession debugSession;
    private final IDebugProtocolServer remoteProxy;
    private final Map<SourceAndUrl, List<BreakpointInfo>> breakpoints;
    private final Map<SourceAndUrl, List<BreakpointInfo>> runToCursorPositions;

    public TLCBreakpointHandler(XDebugSession debugSession, IDebugProtocolServer remoteProxy) {
        super(TLCBreakpointType.class);
        this.debugSession = debugSession;
        this.remoteProxy = remoteProxy;
        breakpoints = new HashMap<>();
        runToCursorPositions = new HashMap<>();
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<TLCBreakpointProperties> breakpoint) {
        SourceAndUrl source = sourceAndUrl(breakpoint);
        synchronized (breakpoints) {
            breakpoints.computeIfAbsent(source, s -> new ArrayList<>())
                       .add(new BreakpointInfo(null, breakpoint));
        }
        updateRemoteBreakpoints();
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<TLCBreakpointProperties> breakpoint,
                                     boolean temporary) {
        SourceAndUrl source = sourceAndUrl(breakpoint);
        synchronized (breakpoints) {
            breakpoints.computeIfAbsent(source, s -> new ArrayList<>())
                       .remove(new BreakpointInfo(null, breakpoint));
        }
        updateRemoteBreakpoints();
    }

    public void registerRunToCursor(XSourcePosition position) {
        Position pos = new Position(
                position.getFile().getUrl(),
                position.getLine(),
                position.getOffset());
        synchronized (runToCursorPositions) {
            runToCursorPositions.computeIfAbsent(sourceAndUrl(pos), s -> new ArrayList<>())
                                .add(new BreakpointInfo(pos, null));
        }
        updateRemoteBreakpoints();
    }

    public void unregisterRunToCursor() {
        synchronized (runToCursorPositions) {
            runToCursorPositions.values().forEach(List::clear);
            if (!runToCursorPositions.isEmpty()) {
                updateRemoteBreakpoints();
            }
        }
    }

    private synchronized void updateRemoteBreakpoints() {
        HashMap<SourceAndUrl, List<BreakpointInfo>> breakpoints;
        breakpoints = new HashMap<>(this.breakpoints);
        breakpoints.putAll(runToCursorPositions);

        Iterator<Entry<SourceAndUrl, List<BreakpointInfo>>> iterator =
                breakpoints.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<SourceAndUrl, List<BreakpointInfo>> entry = iterator.next();
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(entry.getKey().fileUrl);
            if (file == null) {
                continue;
            }
            Document document = ApplicationManager.getApplication().runReadAction(
                    (Computable<Document>) () -> FileDocumentManager.getInstance().getDocument(file));
            if (document == null) {
                continue;
            }
            SetBreakpointsArguments args = new SetBreakpointsArguments();
            args.setSource(entry.getKey().source);

            SourceBreakpoint[] sbps = new SourceBreakpoint[entry.getValue().size()];
            for (int i = 0; i < entry.getValue().size(); i++) {
                BreakpointInfo breakpointInfo = entry.getValue().get(i);
                SourceBreakpoint sbp = new SourceBreakpoint();

                if (breakpointInfo.runToCursorPosition != null) {
                    int line = breakpointInfo.runToCursorPosition.line;
                    // DAP is 1-origin by default
                    sbp.setLine(line + 1);
                    int column = breakpointInfo.runToCursorPosition.offset - document.getLineStartOffset(line);
                    sbp.setColumn(column + 1);
                } else {
                    XLineBreakpoint<TLCBreakpointProperties> breakpoint = breakpointInfo.breakpoint;
                    // TODO: getStartOffset is nullable
                    int line = document.getLineNumber(breakpoint.getProperties().getStartOffset());
                    // DAP is 1-origin by default
                    sbp.setLine(line + 1);
                    if (breakpoint.getProperties().getTextRange() != null) {
                        int column = breakpoint.getProperties().getTextRange().getStartOffset() - document.getLineStartOffset(line);
                        sbp.setColumn(column + 1);
                    }
                }
                sbps[i] = sbp;
            }
            args.setBreakpoints(sbps);

            remoteProxy.setBreakpoints(args).thenAccept(response -> {
                for (int i = 0; i < response.getBreakpoints().length; i++) {
                    BreakpointInfo info = entry.getValue().get(i);
                    Breakpoint res = response.getBreakpoints()[i];
                    if (info.breakpoint != null) {
                        if (res.isVerified()) {
                            debugSession.setBreakpointVerified(info.breakpoint);
                        } else {
                            debugSession.setBreakpointInvalid(info.breakpoint, res.getMessage());
                        }
                    }
                }
            });
        }
        // garbage collection
        this.breakpoints.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        runToCursorPositions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @Value
    private static class SourceAndUrl {
        Source source;
        String fileUrl;
    }

    @Value
    private static class Position {
        String fileUrl;
        int line;
        int offset;
    }

    @Value
    private static class BreakpointInfo {
        // null if this info is for breakpoint
        Position runToCursorPosition;
        // null if this info is for run-to-cursor
        XLineBreakpoint<TLCBreakpointProperties> breakpoint;
    }

    private static SourceAndUrl sourceAndUrl(XLineBreakpoint<TLCBreakpointProperties> breakpoint) {
        Source source = new Source();
        source.setName(breakpoint.getShortFilePath());
        source.setPath(breakpoint.getPresentableFilePath());
        return new SourceAndUrl(source, breakpoint.getFileUrl());
    }

    // Same logic as XLineBreakpointImpl#getPresentableFilePath and getShortFilePath
    private static SourceAndUrl sourceAndUrl(Position position) {
        String url = position.fileUrl;
        String presentableFilePath;
        if (url != null && LocalFileSystem.PROTOCOL.equals(VirtualFileManager.extractProtocol(url))) {
            presentableFilePath = FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(url));
        } else {
            presentableFilePath = "";
        }

        String shortFilePath;
        if (presentableFilePath.isEmpty()) {
            shortFilePath = "";
        } else {
            shortFilePath = new File(presentableFilePath).getName();
        }

        Source source = new Source();
        source.setName(shortFilePath);
        source.setPath(presentableFilePath);
        return new SourceAndUrl(source, url);
    }
}
