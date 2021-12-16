package com.mayreh.intellij.plugin.tlaplus.ide.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFileType;
import com.mayreh.intellij.plugin.util.StringUtil;

import pcal.PlusCalTranslator;
import pcal.PlusCalTranslator.Result;

public class PlusCalTranslateAction extends DumbAwareAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Document document = currentDocument(e);
        e.getPresentation().setEnabled(document != null && document.isWritable());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Document document = currentDocument(e);
        Project project = e.getProject();
        if (project == null || document == null) {
            return;
        }

        FileDocumentManager.getInstance().saveAllDocuments();
        Result result = PlusCalTranslator.translate(document.getText());
        if (result.isSuccess()) {
            WriteCommandAction.runWriteCommandAction(project, (Computable<Void>) () -> {
                document.setText(result.getTranslated());
                return null;
            });
        } else {
            Notification notification =
                    new Notification(
                            "TLA+ plugin",
                            "Failed to translate PlusCal",
                            StringUtil.joinLines(result.getErrors()),
                            NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
        }
    }

    private static @Nullable Document currentDocument(AnActionEvent e) {
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

        return document;
    }
}
