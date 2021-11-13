package com.mayreh.intellij.plugin.tlaplus.psi;

import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;

public interface TLAplusReferenceElement extends PsiElement {
    @Nullable PsiElement getReferenceNameElement();

    @Nullable default String getReferenceName() {
        if (getReferenceNameElement() != null) {
            return getReferenceNameElement().getText();
        }
        return null;
    }
}
