package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;

public interface TLAplusReferenceElement extends PsiElement {
    /**
     * Identifier token of this reference
     */
    @NotNull PsiElement getIdentifier();

    /**
     * Name of this reference.
     * Used for reference resolution.
     */
    @NotNull default String getReferenceName() {
        return StringUtil.defaultIfEmpty(getIdentifier().getText(), "");
    }
}
