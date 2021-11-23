package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;

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
    public @NotNull Stream<? extends TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusReferenceElement reference) {
        if (getNonfixLhs() != null) {
            return getNonfixLhs().getOpDeclList()
                                 .stream()
                                 .map(TLAplusOpDecl::getOpName)
                                 .filter(name -> name != null && !isForwardReference(reference, name));
        }
        return Stream.empty();
    }
}
