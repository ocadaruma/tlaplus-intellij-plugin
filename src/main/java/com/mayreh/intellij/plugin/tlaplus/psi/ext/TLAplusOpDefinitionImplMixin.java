package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;

public abstract class TLAplusOpDefinitionImplMixin extends TLAplusElementImpl implements TLAplusOpDefinition {
    protected TLAplusOpDefinitionImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable TLAplusNamedElement findLocalDefinition(TLAplusReferenceElement element) {
        if (getNonfixLhs() != null) {
            for (TLAplusOpDecl decl : getNonfixLhs().getOpDeclList()) {
                if (decl.getOpName() != null && isLocalDefinitionOf(decl.getOpName(), element, true)) {
                    return decl.getOpName();
                }
            }
        }
        return null;
    }
}
