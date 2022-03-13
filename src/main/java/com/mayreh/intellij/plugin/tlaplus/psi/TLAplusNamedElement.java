package com.mayreh.intellij.plugin.tlaplus.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.mayreh.intellij.plugin.tlaplus.psi.ext.TLAplusElement;

public interface TLAplusNamedElement extends TLAplusElement,
                                             PsiNameIdentifierOwner,
                                             NavigatablePsiElement {
    /**
     * Synonyms for this element's name. (e.g. \leq, =<, <= are all represents same operator)
     * If this element has synonyms, returns the array that contains all synonyms including this element's name.
     * Otherwise, returns null.
     */
    default String @Nullable [] synonyms() {
        return null;
    }

    @NotNull TLAplusNameFixness fixness();
}
