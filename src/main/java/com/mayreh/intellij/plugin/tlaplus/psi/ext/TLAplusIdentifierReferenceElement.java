package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiElement;

public interface TLAplusIdentifierReferenceElement extends TLAplusReferenceElement {
    /**
     * Identifier token of this reference
     */
    @NotNull PsiElement getIdentifier();
}
