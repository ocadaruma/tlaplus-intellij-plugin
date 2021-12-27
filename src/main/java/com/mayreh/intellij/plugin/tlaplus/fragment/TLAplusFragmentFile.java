package com.mayreh.intellij.plugin.tlaplus.fragment;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Key;
import com.intellij.psi.FileViewProvider;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;

import lombok.Value;
import lombok.experimental.Accessors;

public class TLAplusFragmentFile extends PsiFileBase {
    @Value
    @Accessors(fluent = true)
    public static class CodeFragmentContext {
        @NotNull TLAplusModule dummyModule;
        @NotNull Path directory;
    }

    private static final Key<CodeFragmentContext> CONTEXT_KEY =
            Key.create("TLA.code.fragment.context");

    public TLAplusFragmentFile(@NotNull FileViewProvider viewProvider,
                               @NotNull Language language) {
        super(viewProvider, language);
    }

    public void setCodeFragmentContext(@NotNull CodeFragmentContext context) {
        putCopyableUserData(CONTEXT_KEY, context);
    }

    public @Nullable CodeFragmentContext codeFragmentContext() {
        return getCopyableUserData(CONTEXT_KEY);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TLAplusFragmentFileType.INSTANCE;
    }
}
