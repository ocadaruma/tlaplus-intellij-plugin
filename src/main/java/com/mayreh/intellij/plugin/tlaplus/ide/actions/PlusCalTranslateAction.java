package com.mayreh.intellij.plugin.tlaplus.ide.actions;

import org.jetbrains.annotations.NotNull;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.mayreh.intellij.plugin.util.StringUtil;

import pcal.PlusCalTranslator;
import pcal.PlusCalTranslator.Result;

public class PlusCalTranslateAction extends TLAplusActionBase {
    @Override
    protected void doAction(@NotNull AnActionEvent e,
                            @NotNull Project project,
                            @NotNull TLAplusDocument document) {
        FileDocumentManager.getInstance().saveAllDocuments();
        Result result = PlusCalTranslator.translate(document.document().getText());
        if (result.isSuccess()) {
            WriteCommandAction.runWriteCommandAction(project, (Computable<Void>) () -> {
                document.document().setText(result.getTranslated());
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
}
