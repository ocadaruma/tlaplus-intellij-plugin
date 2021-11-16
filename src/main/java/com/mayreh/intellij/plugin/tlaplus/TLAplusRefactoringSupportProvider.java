package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public class TLAplusRefactoringSupportProvider extends RefactoringSupportProvider {
    @Override
    public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
        return element instanceof TLAplusNamedElement;
    }
}
