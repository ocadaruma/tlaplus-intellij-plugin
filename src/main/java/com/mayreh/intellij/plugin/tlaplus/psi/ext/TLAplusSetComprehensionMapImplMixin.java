package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusSetComprehensionMap;

public abstract class TLAplusSetComprehensionMapImplMixin
        extends TLAplusElementImpl implements TLAplusSetComprehensionMap {
    protected TLAplusSetComprehensionMapImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusElement placement) {
        return getBoundNameList().stream().map(Function.identity());
    }
}
