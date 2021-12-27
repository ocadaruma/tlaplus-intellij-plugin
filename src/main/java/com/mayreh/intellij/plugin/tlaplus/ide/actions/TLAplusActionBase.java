package com.mayreh.intellij.plugin.tlaplus.ide.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFileType;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Base class for actions that enabled only when active editor is opening TLA+ files
 */
public abstract class TLAplusActionBase extends DumbAwareAction {
    @Value
    @Accessors(fluent = true)
    public static class TLAplusDocument {
        @NotNull Document document;
        @NotNull TLAplusFile file;
    }

    protected abstract void doAction(@NotNull AnActionEvent e,
                                     @NotNull Project project,
                                     @NotNull TLAplusDocument document);

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(currentDocument(e) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TLAplusDocument document = currentDocument(e);
        Project project = e.getProject();
        if (project == null || document == null) {
            return;
        }

        doAction(e, project, document);
    }

    private static @Nullable TLAplusDocument currentDocument(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return null;
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) {
            editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        }
        if (editor == null) {
            return null;
        }
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null || !file.isInLocalFileSystem() || TLAplusFileType.INSTANCE != file.getFileType()) {
            return null;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (!(psiFile instanceof TLAplusFile)) {
            return null;
        }

        return new TLAplusDocument(document, (TLAplusFile) psiFile);
    }
}
