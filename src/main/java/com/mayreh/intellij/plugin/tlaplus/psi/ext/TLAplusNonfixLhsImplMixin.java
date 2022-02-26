package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNonfixLhs;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpName;

public abstract class TLAplusNonfixLhsImplMixin extends TLAplusElementImpl implements TLAplusNonfixLhs {
    protected TLAplusNonfixLhsImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull TLAplusNamedElement getNamedElement() {
        return getNonfixLhsName();
    }

    @Override
    public @NotNull Stream<TLAplusOpName> opNames() {
        return getOpDeclList()
                .stream()
                .map(TLAplusOpDecl::getOpName)
                .filter(Objects::nonNull);
    }
}
