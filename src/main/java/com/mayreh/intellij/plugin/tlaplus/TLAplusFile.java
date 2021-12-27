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
    /**
     * Store the directory that the file "virtually" belongs to.
     *
     * If this file is created for dummy module to evaluate expression,
     * corresponding file on file-system may not exist.
     * It would be a problem when searching a reference from other modules in same directory.
     * So we assume this dummy module "virtually" belongs to same directory as
     * the module that the expression will be evaluated on by storing explicitly in UserData.
     */
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
