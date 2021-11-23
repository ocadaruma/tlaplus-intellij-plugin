package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusQuantifierExpr;

public abstract class TLAplusQuantifierExprImplMixin extends TLAplusElementImpl implements TLAplusQuantifierExpr {
    protected TLAplusQuantifierExprImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<? extends TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusReferenceElement reference) {
        return getBoundNameList()
                .stream()
                .filter(e -> !isForwardReference(reference, e));
    }
}
