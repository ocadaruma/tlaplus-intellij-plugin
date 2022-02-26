package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiFactory;

public abstract class TLAplusIdentifierNamedElementImplMixin extends TLAplusNamedElementImplBase {
    protected TLAplusIdentifierNamedElementImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    public abstract PsiElement getIdentifier();

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        PsiElement identifier = getIdentifier();
        if (identifier != null) {
            identifier.replace(new TLAplusPsiFactory(getProject()).createIdentifier(name));
        }
        return this;
    }
}
