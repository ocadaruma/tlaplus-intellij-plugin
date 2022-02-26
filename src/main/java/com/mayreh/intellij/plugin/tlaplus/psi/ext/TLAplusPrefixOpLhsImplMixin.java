package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPrefixOpLhs;

public abstract class TLAplusPrefixOpLhsImplMixin extends TLAplusElementImpl implements TLAplusPrefixOpLhs {
    protected TLAplusPrefixOpLhsImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull TLAplusNamedElement getNamedElement() {
        return getPrefixOpName();
    }

    @Override
    public @NotNull Stream<TLAplusOpName> opNames() {
        return Stream.of(getOpName());
    }
}
