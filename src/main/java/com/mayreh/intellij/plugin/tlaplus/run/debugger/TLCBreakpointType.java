package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFileType;

public class TLCBreakpointType extends XLineBreakpointType<TLCBreakpointProperties> {
    public TLCBreakpointType() {
        super("TLCBreakpointType", "TLC Breakpoint");
    }

    @Override
    public @Nullable TLCBreakpointProperties createProperties() {
        return new TLCBreakpointProperties();
    }

    @Override
    public @Nullable TLCBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        return new TLCBreakpointProperties();
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        if (!TLAplusFileType.INSTANCE.getName().equals(file.getFileType().getName())) {
            return false;
        }

        // Taken from JavaLineBreakpointTypeBase.canPutAtElement
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return false;
        }
        Ref<Boolean> canPut = new Ref<>(false);

        // We only sanity check that the line is not a comment or whitespace.
        // More complete check will be done when registering breakpoint after TLC debugger starts up.
        XDebuggerUtil.getInstance().iterateLine(project, document, line, element -> {
            if (element instanceof PsiComment) {
                return true;
            }
            if (element instanceof PsiWhiteSpace) {
                return true;
            }
            canPut.set(true);
            return false;
        });

        return canPut.get();
    }

    @Override
    public @NotNull List<? extends XLineBreakpointType<TLCBreakpointProperties>.XLineBreakpointVariant> computeVariants(
            @NotNull Project project, @NotNull XSourcePosition position) {
        List<XLinePsiElementBreakpointVariant> variants = new ArrayList<>();

        if (!TLAplusFileType.INSTANCE.getName().equals(position.getFile().getFileType().getName())) {
            return variants;
        }
        Document document = FileDocumentManager.getInstance().getDocument(position.getFile());
        if (document == null) {
            return variants;
        }
        XDebuggerUtil.getInstance().iterateLine(project, document, position.getLine(), element -> {
            if (element instanceof PsiComment) {
                return true;
            }
            if (element instanceof PsiWhiteSpace) {
                return true;
            }
            XSourcePosition pos = XDebuggerUtil.getInstance().createPositionByElement(element);
            variants.add(new XLinePsiElementBreakpointVariant(pos, element) {
                @Override
                public @Nullable TLCBreakpointProperties createProperties() {
                    TLCBreakpointProperties properties = new TLCBreakpointProperties();
                    properties.setTextRange(element.getTextRange());
                    return properties;
                }
            });
//            });
            return true;
        });

        return variants;
    }

    @Override
    public @Nullable TextRange getHighlightRange(XLineBreakpoint<TLCBreakpointProperties> breakpoint) {
        return breakpoint.getProperties().getTextRange();
    }

    @Override
    public XSourcePosition getSourcePosition(@NotNull XBreakpoint<TLCBreakpointProperties> breakpoint) {
        if (!(breakpoint instanceof XLineBreakpoint<?>)) {
            return null;
        }
        XLineBreakpoint<?> delegate = (XLineBreakpoint<?>) breakpoint;
        VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(delegate.getFileUrl());
        if (file == null) {
            return null;
        }
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }

        Ref<PsiElement> elementRef = new Ref<>();
        XDebuggerUtil.getInstance().iterateLine(
                ProjectUtil.guessProjectForFile(file), document, delegate.getLine(), element -> {
                    if (element.getTextRange().equals(breakpoint.getProperties().getTextRange())) {
                        elementRef.set(element);
                        return false;
                    }
                    return true;
                });
        if (elementRef.isNull()) {
            return null;
        }
        return new TLCXSourcePosition(file, elementRef.get().getTextRange());
    }
}
