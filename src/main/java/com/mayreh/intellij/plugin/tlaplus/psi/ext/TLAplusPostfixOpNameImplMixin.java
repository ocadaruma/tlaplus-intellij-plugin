package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNameFixness;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPostfixOpName;

public abstract class TLAplusPostfixOpNameImplMixin extends TLAplusNamedElementImplBase implements TLAplusPostfixOpName {
    protected TLAplusPostfixOpNameImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getPostfixOp();
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        throw new IncorrectOperationException("Can't rename symbolic operator");
    }

    @Override
    public @NotNull TLAplusNameFixness fixness() {
        return TLAplusNameFixness.POSTFIX;
    }
}
