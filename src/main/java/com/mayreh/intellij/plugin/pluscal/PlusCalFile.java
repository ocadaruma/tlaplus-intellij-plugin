package com.mayreh.intellij.plugin.pluscal;

import org.jetbrains.annotations.NotNull;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

public class PlusCalFile extends PsiFileBase {
    public PlusCalFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, PlusCalLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return PlusCalFileType.INSTANCE;
    }
}
