package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPostfixOpName;

public abstract class TLAplusPostfixOpNameImpl extends TLAplusNamedElementImplBase implements TLAplusPostfixOpName {
    protected TLAplusPostfixOpNameImpl(@NotNull ASTNode node) {
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
}
