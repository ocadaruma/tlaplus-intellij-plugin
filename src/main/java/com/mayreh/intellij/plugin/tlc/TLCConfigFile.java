package com.mayreh.intellij.plugin.tlc;

import org.jetbrains.annotations.NotNull;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

public class TLCConfigFile extends PsiFileBase {
    public TLCConfigFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, TLCConfigLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TLCConfigFileType.INSTANCE;
    }
}
