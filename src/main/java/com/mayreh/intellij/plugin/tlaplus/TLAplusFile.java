package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.NotNull;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

public class TLAplusFile extends PsiFileBase {
    public TLAplusFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, TLAplusLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TLAplusFileType.INSTANCE;
    }
}
