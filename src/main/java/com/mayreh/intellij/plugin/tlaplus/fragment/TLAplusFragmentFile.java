package com.mayreh.intellij.plugin.tlaplus.fragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Key;
import com.intellij.psi.FileViewProvider;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;

public class TLAplusFragmentFile extends PsiFileBase {
    private static final Key<TLAplusModule> CONTEXT_KEY =
            Key.create("TLA.fragment.module");

    public TLAplusFragmentFile(@NotNull FileViewProvider viewProvider,
                               @NotNull Language language) {
        super(viewProvider, language);
    }

    public void setModule(@NotNull TLAplusModule module) {
        putCopyableUserData(CONTEXT_KEY, module);
    }

    public @Nullable TLAplusModule module() {
        return getCopyableUserData(CONTEXT_KEY);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TLAplusFragmentFileType.INSTANCE;
    }
}
