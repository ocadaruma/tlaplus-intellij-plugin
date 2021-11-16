package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusLetExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;

public abstract class TLAplusLetExprImplMixin extends TLAplusElementImpl implements TLAplusLetExpr {
    protected TLAplusLetExprImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable TLAplusNamedElement findDefinition(TLAplusReferenceElement element) {
        for (TLAplusOpDefinition opDef : getOpDefinitionList()) {
            if (opDef.getNonfixLhs() != null) {
                if (isDefinitionOf(opDef.getNonfixLhs().getNonfixLhsName(), element, true)) {
                    return opDef.getNonfixLhs().getNonfixLhsName();
                }
            }
        }
        for (TLAplusFuncDefinition funcDef : getFuncDefinitionList()) {
            if (isDefinitionOf(funcDef.getFuncName(), element, true)) {
                return funcDef.getFuncName();
            }
        }
        return null;
    }
}
