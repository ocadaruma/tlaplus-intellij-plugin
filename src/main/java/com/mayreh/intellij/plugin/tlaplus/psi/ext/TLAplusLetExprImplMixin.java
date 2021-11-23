package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusLetExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNonfixLhs;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNonfixLhsName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;

public abstract class TLAplusLetExprImplMixin extends TLAplusElementImpl implements TLAplusLetExpr {
    protected TLAplusLetExprImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusReferenceElement reference) {
        Stream<TLAplusNonfixLhsName> ops = getOpDefinitionList()
                .stream()
                .map(TLAplusOpDefinition::getNonfixLhs)
                .filter(Objects::nonNull)
                .map(TLAplusNonfixLhs::getNonfixLhsName)
                .filter(name -> !isForwardReference(reference, name));

        Stream<TLAplusFuncName> functions = getFuncDefinitionList()
                .stream()
                .map(TLAplusFuncDefinition::getFuncName)
                .filter(e -> !isForwardReference(reference, e));

        Stream<TLAplusNonfixLhsName> modules = getModuleDefinitionList()
                .stream()
                .map(e -> e.getNonfixLhs().getNonfixLhsName())
                .filter(name -> !isForwardReference(reference, name));

        return Stream.concat(Stream.concat(ops, functions), modules);
    }
}
