package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInfixOpLhs;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpName;

public abstract class TLAplusInfixOpLhsImplMixin extends TLAplusElementImpl implements TLAplusInfixOpLhs {
    protected TLAplusInfixOpLhsImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull TLAplusNamedElement getNamedElement() {
        return getInfixOpName();
    }

    @Override
    public @NotNull Stream<TLAplusOpName> opNames() {
        return getOpNameList().stream();
    }
}
