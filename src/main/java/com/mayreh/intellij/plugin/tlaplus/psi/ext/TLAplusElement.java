package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusCodeFragment;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;

public interface TLAplusElement extends PsiElement {
    /**
     * The module that the element belongs to.
     */
    default @Nullable TLAplusModule currentModule() {
        // For ordinary TLA+ spec, every element should be inside TLAplusModule
        TLAplusModule parentModule = PsiTreeUtil.getParentOfType(this, TLAplusModule.class);
        if (parentModule != null) {
            return parentModule;
        }
        // For TLA+ code fragment, root element is TLAplusCodeFragment rather than TLAplusModule
        TLAplusCodeFragment parentFragment = PsiTreeUtil.getParentOfType(this, TLAplusCodeFragment.class);
        if (parentFragment != null) {
            return parentFragment.currentModule();
        }
        return null;
    }
}
