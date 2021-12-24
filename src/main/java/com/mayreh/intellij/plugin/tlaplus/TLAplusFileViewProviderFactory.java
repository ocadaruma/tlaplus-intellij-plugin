package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;

public class TLAplusFileViewProviderFactory implements FileViewProviderFactory {
    @Override
    public @NotNull FileViewProvider createFileViewProvider(
            @NotNull VirtualFile file,
            Language language,
            @NotNull PsiManager manager,
            boolean eventSystemEnabled) {
        if ("__DUMMY__.tla".equals(file.getName())) {
            return new DummyViewProvider(manager, file, eventSystemEnabled);
        }
        return new SingleRootFileViewProvider(manager, file, eventSystemEnabled);
    }

    private static class DummyViewProvider extends SingleRootFileViewProvider {
        DummyViewProvider(@NotNull PsiManager manager,
                          @NotNull VirtualFile virtualFile,
                          boolean eventSystemEnabled) {
            super(manager, virtualFile, eventSystemEnabled);
        }

        @Override
        public @NotNull CharSequence getContents() {
            CharSequence content = super.getContents();
            return "---- MODULE " + content;
        }
    }
}
