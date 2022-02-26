package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleHeader;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleRef;

public abstract class TLAplusModuleRefImplMixin extends TLAplusElementImpl implements TLAplusModuleRef {
    protected TLAplusModuleRefImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new TLAplusReference(this, e -> e instanceof TLAplusModuleHeader);
    }

    @Override
    public @NotNull String getReferenceName() {
        return getIdentifier().getText();
    }
}
