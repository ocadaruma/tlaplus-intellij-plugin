package com.mayreh.intellij.plugin.tlaplus;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Key;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;

public class TLAplusFile extends PsiFileBase {
    private static final Key<PsiDirectory> DIRECTORY_KEY =
            Key.create("TLA.directory");

    public TLAplusFile(@NotNull FileViewProvider viewProvider,
                       @NotNull Language language) {
        super(viewProvider, language);
    }

    public void setDirectory(@NotNull PsiDirectory directory) {
        putCopyableUserData(DIRECTORY_KEY, directory);
    }

    public @Nullable PsiDirectory directory() {
        return getCopyableUserData(DIRECTORY_KEY);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TLAplusFileType.INSTANCE;
    }
}
