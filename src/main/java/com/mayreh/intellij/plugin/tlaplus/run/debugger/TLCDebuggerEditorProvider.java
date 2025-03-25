package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFileType;

public class TLCDebuggerEditorProvider extends XDebuggerEditorsProviderBase {
    @Override
    protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text,
                                                   @Nullable PsiElement context, boolean isPhysical) {
        return TLAplusFragmentFile.createFragmentFile(project, text);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TLAplusFragmentFileType.INSTANCE;
    }
}
