package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusDashdotOpLhs;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpName;

public abstract class TLAplusDashdotOpLhsImplMixin extends TLAplusElementImpl implements TLAplusDashdotOpLhs {
    protected TLAplusDashdotOpLhsImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull TLAplusNamedElement getNamedElement() {
        return getDashdotOpName();
    }

    @Override
    public @NotNull Stream<TLAplusOpName> opNames() {
        return Stream.of(getOpName());
    }
}
