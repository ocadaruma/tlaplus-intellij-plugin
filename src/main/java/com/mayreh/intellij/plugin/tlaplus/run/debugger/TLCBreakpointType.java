package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFileType;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;

import lombok.RequiredArgsConstructor;

public class TLCBreakpointType extends XLineBreakpointType<TLCBreakpointProperties> {
    public TLCBreakpointType() {
        super("TLCBreakpointType", "TLC Breakpoints");
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

        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return false;
        }
        Ref<Boolean> canPut = new Ref<>(false);

        // We only sanity check that the line corresponds to any AST node except comment and whitespace.
        // More complete check (e.g. hitCount) will be done when registering breakpoint after TLC debugger starts up.
        XDebuggerUtil.getInstance().iterateLine(project, document, line, element -> {
            if (element instanceof PsiComment ||
                element instanceof PsiWhiteSpace) {
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
        List<XLineBreakpointVariant> variants = new ArrayList<>();

        if (!TLAplusFileType.INSTANCE.getName().equals(position.getFile().getFileType().getName())) {
            return variants;
        }
        Document document = FileDocumentManager.getInstance().getDocument(position.getFile());
        if (document == null) {
            return variants;
        }

        TextRange lineRange = TextRange.create(
                document.getLineStartOffset(position.getLine()),
                document.getLineEndOffset(position.getLine()));
        Ref<TextRange> opDefNameRange = Ref.create(TextRange.EMPTY_RANGE);
        Ref<Boolean> hitLine = Ref.create(false);
        XDebuggerUtil.getInstance().iterateLine(project, document, position.getLine(), element -> {
            if (element instanceof PsiComment ||
                element instanceof PsiWhiteSpace) {
                return true;
            }

            // Any non-comment, non-whitespace element is a candidate for breakpoint
            hitLine.set(true);

            TLAplusOpDefinition opDef = PsiTreeUtil.getParentOfType(
                    element,
                    TLAplusOpDefinition.class,
                    false,
                    lineRange.getStartOffset());
            if (opDef != null) {
                TLAplusNamedElement name = null;
                if (opDef.getDashdotOpLhs() != null) {
                    name = opDef.getDashdotOpLhs().getDashdotOpName();
                } else if (opDef.getNonfixLhs() != null) {
                    name = opDef.getNonfixLhs().getNonfixLhsName();
                } else if (opDef.getInfixOpLhs() != null) {
                    name = opDef.getInfixOpLhs().getInfixOpName();
                } else if (opDef.getPostfixOpLhs() != null) {
                    name = opDef.getPostfixOpLhs().getPostfixOpName();
                } else if (opDef.getPrefixOpLhs() != null) {
                    name = opDef.getPrefixOpLhs().getPrefixOpName();
                } else {
                    // Unexpected!!
                }

                if (name != null) {
                    // (Unlikely though) when there are multiple opdef in single line (which is syntactically possible),
                    // we take only first opdef for simplicity (e.g. how to handle multiple text ranges for multiple opdef..)
                    // and return immediately.
                    opDefNameRange.set(name.getTextRange());
                    return false;
                }
            }

            return true;
        });
        if (hitLine.get()) {
            variants.add(new TextRangeBreakpointVariant("Expression", null));
        }
        if (!opDefNameRange.get().isEmpty()) {
            variants.add(new TextRangeBreakpointVariant("Action/Spec", opDefNameRange.get()));
        }

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

        return new TLCXSourcePosition(file,
                                      ((XLineBreakpoint<TLCBreakpointProperties>) breakpoint).getLine(),
                                      breakpoint.getProperties().getTextRange());
    }

    @Override
    public boolean canBeHitInOtherPlaces() {
        // Action/Spec breakpoint can be hit in other place on state generation
        return true;
    }

    @RequiredArgsConstructor
    private class TextRangeBreakpointVariant extends XLineBreakpointVariant {
        private final String text;
        private final TextRange range;

        @Override
        public @NotNull @Nls String getText() {
            return text;
        }

        @Override
        public @Nullable Icon getIcon() {
            return null;
        }

        @Override
        public @Nullable TextRange getHighlightRange() {
            return range;
        }

        @Override
        public @Nullable TLCBreakpointProperties createProperties() {
            TLCBreakpointProperties properties = new TLCBreakpointProperties();
            if (range != null) {
                properties.setTextRange(range);
            }
            return properties;
        }
    }
}
