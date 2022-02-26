package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusUnqualifiedPostfixOp;

public abstract class TLAplusUnqualifiedPostfixOpImplMixin extends TLAplusElementImpl implements TLAplusUnqualifiedPostfixOp {
    protected TLAplusUnqualifiedPostfixOpImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new TLAplusReference(this, e -> true);
    }

    @Override
    public @NotNull String getReferenceName() {
        return getPostfixOp().getText();
    }
}
