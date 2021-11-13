package com.mayreh.intellij.plugin.tlaplus.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiPolyVariantReferenceBase;

public abstract class TLAplusReferenceBase<T extends TLAplusReferenceElement>
        extends PsiPolyVariantReferenceBase<T> {
    protected TLAplusReferenceBase(@NotNull T psiElement) {
        super(psiElement);
    }


}
