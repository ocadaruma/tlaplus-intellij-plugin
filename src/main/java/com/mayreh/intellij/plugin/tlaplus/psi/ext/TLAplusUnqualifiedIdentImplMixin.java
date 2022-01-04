package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusUnqualifiedIdent;

public abstract class TLAplusUnqualifiedIdentImplMixin extends TLAplusElementImpl implements TLAplusUnqualifiedIdent {
    protected TLAplusUnqualifiedIdentImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new TLAplusReference(this, e -> true);
    }
}
