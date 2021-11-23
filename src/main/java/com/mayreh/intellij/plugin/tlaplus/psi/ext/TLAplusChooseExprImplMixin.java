package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusChooseExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public abstract class TLAplusChooseExprImplMixin extends TLAplusElementImpl implements TLAplusChooseExpr {
    protected TLAplusChooseExprImplMixin(@NotNull ASTNode node) {
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
