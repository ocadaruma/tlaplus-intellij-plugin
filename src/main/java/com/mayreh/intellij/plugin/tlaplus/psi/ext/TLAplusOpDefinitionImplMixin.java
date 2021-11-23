package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;

import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;

public abstract class TLAplusOpDefinitionImplMixin extends TLAplusElementImpl implements TLAplusOpDefinition {
    protected TLAplusOpDefinitionImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusElement placement) {
        if (getNonfixLhs() != null) {
            return getNonfixLhs().getOpDeclList()
                                 .stream()
                                 .map(TLAplusOpDecl::getOpName)
                                 .filter(name -> name != null && !isForwardReference(placement, name))
                                 .map(Function.identity());
        }
        return Stream.empty();
    }
}
