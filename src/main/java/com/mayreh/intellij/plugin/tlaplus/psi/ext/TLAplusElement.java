package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;

public interface TLAplusElement extends PsiElement {
    /**
     * The module that the element belongs to.
     */
    default @Nullable TLAplusModule currentModule() {
        return PsiTreeUtil.getParentOfType(this, TLAplusModule.class);
    }
}
