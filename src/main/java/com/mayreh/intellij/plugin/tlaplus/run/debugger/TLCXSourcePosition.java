package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.nio.file.Paths;

import org.eclipse.lsp4j.debug.StackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XDebuggerUtilImpl;
import com.intellij.xdebugger.impl.ui.ExecutionPointHighlighter.HighlighterProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCXSourcePosition implements XSourcePosition, HighlighterProvider {
    private static final Logger log = Logger.getInstance(TLCXSourcePosition.class);

    private final VirtualFile file;
    private final int line;
    private final TextRange textRange;

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getOffset() {
        return textRange.getStartOffset();
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
        return textRange;
    }

    public static @Nullable TLCXSourcePosition createByStackFrame(StackFrame dapStackFrame) {
        VirtualFile file = VfsUtil.findFile(Paths.get(dapStackFrame.getSource().getPath()), true);
        if (file == null) {
            log.warn("Cannot find virtual file for " + dapStackFrame.getSource().getPath());
            return null;
        }
        Document document = ApplicationManager.getApplication().runReadAction(
                (Computable<Document>) () -> FileDocumentManager.getInstance().getDocument(file));
        if (document == null) {
            log.warn("Cannot find document for " + file.getPath());
            return null;
        }
        // DAP stack frame line/col are 1-based
        int startOffset = document.getLineStartOffset(dapStackFrame.getLine() - 1) + dapStackFrame.getColumn() - 1;
        int endOffset = document.getLineStartOffset(dapStackFrame.getEndLine() - 1) + dapStackFrame.getEndColumn() - 1;

        return new TLCXSourcePosition(file, dapStackFrame.getLine() - 1, new TextRange(startOffset, endOffset));
    }
}
