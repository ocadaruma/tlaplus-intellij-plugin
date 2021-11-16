package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusConstantDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusVariableDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusVariableName;

public abstract class TLAplusModuleImplMixin extends TLAplusElementImpl implements TLAplusModule {
    protected TLAplusModuleImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable TLAplusNamedElement findDefinition(TLAplusReferenceElement element) {
        for (TLAplusVariableDecl decl : getVariableDeclList()) {
            for (TLAplusVariableName variableName : decl.getVariableNameList()) {
                if (isDefinitionOf(variableName, element, true)) {
                    return variableName;
                }
            }
        }
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
        for (TLAplusConstantDecl decl : getConstantDeclList()) {
            for (TLAplusOpDecl opDecl : decl.getOpDeclList()) {
                if (opDecl.getOpName() != null) {
                    if (isDefinitionOf(opDecl.getOpName(), element, true)) {
                        return opDecl.getOpName();
                    }
                }
            }
        }
        return null;
    }
}
