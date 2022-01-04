package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;

import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFunctionLiteralExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public abstract class TLAplusFunctionLiteralExprImplMixin extends TLAplusElementImpl implements TLAplusFunctionLiteralExpr {
    protected TLAplusFunctionLiteralExprImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusElement placement) {
        return getBoundNameList()
                .stream()
                .filter(e -> !isForwardReference(placement, e))
                .map(Function.identity());
    }
}
