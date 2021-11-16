package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpArgName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;

public abstract class TLAplusOpDefinitionImplMixin extends TLAplusElementImpl implements TLAplusOpDefinition {
    protected TLAplusOpDefinitionImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable TLAplusNamedElement findDefinition(TLAplusReferenceElement element) {
        if (getNonfixLhs() != null) {
            for (TLAplusOpArgName argName : getNonfixLhs().getOpArgNameList()) {
                if (isDefinitionOf(argName, element, true)) {
                    return argName;
                }
            }
        }
        return null;
    }
}
