package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNameFixness;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusUnqualifiedInfixOp;

public abstract class TLAplusUnqualifiedInfixOpImplMixin extends TLAplusElementImpl implements TLAplusUnqualifiedInfixOp {
    protected TLAplusUnqualifiedInfixOpImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new TLAplusReference(this, e -> true);
    }

    @Override
    public @NotNull String getReferenceName() {
        return getText();
    }

    @Override
    public @NotNull TLAplusNameFixness fixness() {
        return TLAplusNameFixness.INFIX;
    }
}
