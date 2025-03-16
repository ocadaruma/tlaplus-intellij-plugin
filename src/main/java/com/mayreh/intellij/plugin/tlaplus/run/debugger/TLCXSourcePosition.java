package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XDebuggerUtilImpl;
import com.intellij.xdebugger.impl.ui.ExecutionPointHighlighter.HighlighterProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCXSourcePosition implements XSourcePosition, HighlighterProvider {
    private final VirtualFile file;
    private final PsiElement element;

    @Override
    public int getLine() {
        return FileDocumentManager.getInstance().getDocument(file).getLineNumber(element.getTextOffset());
    }

    @Override
    public int getOffset() {
        return element.getTextOffset();
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }

    @Override
    public @NotNull Navigatable createNavigatable(@NotNull Project project) {
        return XDebuggerUtilImpl.createNavigatable(project, this);
    }

    @Override
    public @Nullable TextRange getHighlightRange() {
        return element.getTextRange();
    }
}
