package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusSubstitutingIdent;

public abstract class TLAplusSubstitutingIdentImplMixin extends TLAplusElementImpl implements TLAplusSubstitutingIdent {
    protected TLAplusSubstitutingIdentImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        // TODO: Filter only substitutable names
        return new TLAplusReference(this, e -> true);
    }
}
