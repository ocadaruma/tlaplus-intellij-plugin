package com.mayreh.intellij.plugin.tlaplus.run.debugger;

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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

import lombok.Value;

public class TLCExceptionBreakpointHandler extends XBreakpointHandler<XBreakpoint<TLCExceptionBreakpointProperties>> {
    private final XDebugSession debugSession;
    private final IDebugProtocolServer remoteProxy;
    private final Map<SourceAndUrl, List<XBreakpoint<TLCExceptionBreakpointProperties>>> breakpoints;

    public TLCExceptionBreakpointHandler(XDebugSession debugSession, IDebugProtocolServer remoteProxy) {
        super(TLCExceptionBreakpointType.class);
        this.debugSession = debugSession;
        this.remoteProxy = remoteProxy;
        breakpoints = new HashMap<>();
    }

    @Override
    public void registerBreakpoint(@NotNull XBreakpoint<TLCExceptionBreakpointProperties> breakpoint) {
//        SourceAndUrl source = sourceAndUrl(breakpoint);
//        synchronized (breakpoints) {
//            breakpoints.computeIfAbsent(source, s -> new ArrayList<>())
//                       .add(breakpoint);
//        }
        updateBreakpoints();
    }

    @Override
    public void unregisterBreakpoint(@NotNull XBreakpoint<TLCExceptionBreakpointProperties> breakpoint,
                                     boolean temporary) {
//        SourceAndUrl source = sourceAndUrl(breakpoint);
//        synchronized (breakpoints) {
//            breakpoints.computeIfAbsent(source, s -> new ArrayList<>())
//                       .remove(breakpoint);
//        }
        updateBreakpoints();
    }

    private void updateBreakpoints() {
//        synchronized (breakpoints) {
//            Iterator<Entry<SourceAndUrl, List<XLineBreakpoint<TLCBreakpointProperties>>>> iterator =
//                    breakpoints.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Entry<SourceAndUrl, List<XLineBreakpoint<TLCBreakpointProperties>>> entry = iterator.next();
//                VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(entry.getKey().fileUrl);
//                if (file == null) {
//                    continue;
//                }
//                Document document = FileDocumentManager.getInstance().getDocument(file);
//                if (document == null) {
//                    continue;
//                }
//                SetBreakpointsArguments args = new SetBreakpointsArguments();
//                args.setSource(entry.getKey().source);
//
//                SourceBreakpoint[] sbps = new SourceBreakpoint[entry.getValue().size()];
//                for (int i = 0; i < entry.getValue().size(); i++) {
//                    XLineBreakpoint<TLCBreakpointProperties> breakpoint = entry.getValue().get(i);
//                    SourceBreakpoint sbp = new SourceBreakpoint();
//
//                    // TODO: getStartOffset is nullable
//                    int line = document.getLineNumber(breakpoint.getProperties().getStartOffset());
//                    // DAP is 1-origin by default
//                    sbp.setLine(line + 1);
//                    if (breakpoint.getProperties().getTextRange() != null) {
//                        int column = breakpoint.getProperties().getTextRange().getStartOffset() - document.getLineStartOffset(line);
//                        sbp.setColumn(column + 1);
//                    }
//                    sbps[i] = sbp;
//                }
//                args.setBreakpoints(sbps);
//
//                remoteProxy.setBreakpoints(args).thenAccept(response -> {
//                    for (int i = 0; i < response.getBreakpoints().length; i++) {
//                        XLineBreakpoint<TLCBreakpointProperties> b = entry.getValue().get(i);
//                        Breakpoint res = response.getBreakpoints()[i];
//                        if (res.isVerified()) {
//                            debugSession.setBreakpointVerified(b);
//                        } else {
//                            debugSession.setBreakpointInvalid(b, res.getMessage());
//                        }
//                    }
//                });
//
//                if (entry.getValue().isEmpty()) {
//                    iterator.remove();
//                }
//            }
//        }
    }

    @Value
    private static class SourceAndUrl {
        Source source;
        String fileUrl;
    }

    private static SourceAndUrl sourceAndUrl(XLineBreakpoint<TLCBreakpointProperties> breakpoint) {
        Source source = new Source();
        source.setName(breakpoint.getShortFilePath());
        source.setPath(breakpoint.getPresentableFilePath());
        return new SourceAndUrl(source, breakpoint.getFileUrl());
    }
}
