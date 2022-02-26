package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

/**
 * Base class for the element that defines name
 */
public abstract class TLAplusNamedElementImplBase extends TLAplusElementImpl implements TLAplusNamedElement {
    protected TLAplusNamedElementImplBase(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public int getTextOffset() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getTextOffset() : super.getTextOffset();
    }

    @Override
    public String getName() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getText() : super.getName();
    }
}
