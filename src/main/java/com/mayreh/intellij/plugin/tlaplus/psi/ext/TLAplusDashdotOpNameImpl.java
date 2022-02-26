package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusDashdotOpName;

public abstract class TLAplusDashdotOpNameImpl extends TLAplusNamedElementImplBase implements TLAplusDashdotOpName {
    protected TLAplusDashdotOpNameImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getOpDashdot();
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        throw new IncorrectOperationException("Can't rename symbolic operator");
    }
}
