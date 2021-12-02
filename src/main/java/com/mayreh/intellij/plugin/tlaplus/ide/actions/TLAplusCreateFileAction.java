package com.mayreh.intellij.plugin.tlaplus.ide.actions;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

public class TLAplusCreateFileAction extends CreateFileFromTemplateAction {
    public TLAplusCreateFileAction() {
        super("TLA+ Spec", "Creates new TLA+ Spec", AllIcons.FileTypes.Any_type);
    }

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory,
                               @NotNull CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle("New TLA+ Spec")
               .addKind("TLA+ Spec", AllIcons.FileTypes.Any_type, "TLA+ Spec")
               .addKind("PlusCal Algorithm", AllIcons.FileTypes.Any_type, "PlusCal Algorithm");
    }

    @Override
    protected String getActionName(PsiDirectory directory, @NonNls @NotNull String newName,
                                   @NonNls String templateName) {
        return "TLA+ Spec";
    }
}
