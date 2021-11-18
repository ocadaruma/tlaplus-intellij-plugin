package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusConstantDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleRef;
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
    public @Nullable TLAplusNamedElement findLocalDefinition(TLAplusReferenceElement element) {
        for (TLAplusVariableDecl decl : getVariableDeclList()) {
            for (TLAplusVariableName variableName : decl.getVariableNameList()) {
                if (isLocalDefinitionOf(variableName, element, true)) {
                    return variableName;
                }
            }
        }
        for (TLAplusOpDefinition opDef : getOpDefinitionList()) {
            if (opDef.getNonfixLhs() != null) {
                if (isLocalDefinitionOf(opDef.getNonfixLhs().getNonfixLhsName(), element, true)) {
                    return opDef.getNonfixLhs().getNonfixLhsName();
                }
            }
        }
        for (TLAplusFuncDefinition funcDef : getFuncDefinitionList()) {
            if (isLocalDefinitionOf(funcDef.getFuncName(), element, true)) {
                return funcDef.getFuncName();
            }
        }
        for (TLAplusConstantDecl decl : getConstantDeclList()) {
            for (TLAplusOpDecl opDecl : decl.getOpDeclList()) {
                if (opDecl.getOpName() != null) {
                    if (isLocalDefinitionOf(opDecl.getOpName(), element, true)) {
                        return opDecl.getOpName();
                    }
                }
            }
        }
        for (TLAplusModuleDefinition moduleDef : getModuleDefinitionList()) {
            if (isLocalDefinitionOf(moduleDef.getNonfixLhs().getNonfixLhsName(), element, true)) {
                return moduleDef.getNonfixLhs().getNonfixLhsName();
            }
        }
        // Find exported definitions by EXTENDS
        for (TLAplusModuleRef extend : getModuleRefList()) {
            String moduleName = extend.getReferenceName();
            TLAplusModule module = findModule(moduleName);
            if (module != null) {
                TLAplusNamedElement definition = module.findPublicDefinition(element.getReferenceName());
                if (definition != null) {
                    return definition;
                }
            }
        }
        // TODO: Find exported definitions by INSTANCE
        return null;
    }

    @Override
    public @Nullable TLAplusNamedElement findPublicDefinition(String referenceName) {
        for (TLAplusVariableDecl decl : getVariableDeclList()) {
            for (TLAplusVariableName variableName : decl.getVariableNameList()) {
                if (findPublic(decl, variableName, referenceName)) {
                    return variableName;
                }
            }
        }
        for (TLAplusConstantDecl decl : getConstantDeclList()) {
            for (TLAplusOpDecl opDecl : decl.getOpDeclList()) {
                if (opDecl.getOpName() != null) {
                    if (findPublic(decl, opDecl.getOpName(), referenceName)) {
                        return opDecl.getOpName();
                    }
                }
            }
        }
        for (TLAplusOpDefinition opDef : getOpDefinitionList()) {
            if (opDef.getNonfixLhs() != null) {
                if (findPublic(opDef, opDef.getNonfixLhs().getNonfixLhsName(), referenceName)) {
                    return opDef.getNonfixLhs().getNonfixLhsName();
                }
            }
        }
        for (TLAplusFuncDefinition funcDef : getFuncDefinitionList()) {
            if (findPublic(funcDef, funcDef.getFuncName(), referenceName)) {
                return funcDef.getFuncName();
            }
        }
        for (TLAplusModuleDefinition moduleDef : getModuleDefinitionList()) {
            if (findPublic(moduleDef, moduleDef.getNonfixLhs().getNonfixLhsName(), referenceName)) {
                return moduleDef.getNonfixLhs().getNonfixLhsName();
            }
        }
        // Find exported definitions by EXTENDS
        for (TLAplusModuleRef extend : getModuleRefList()) {
            String moduleName = extend.getReferenceName();
            TLAplusModule module = findModule(moduleName);
            if (module != null) {
                TLAplusNamedElement definition = module.findPublicDefinition(referenceName);
                if (definition != null) {
                    return definition;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable TLAplusModule resolveModulePublic(String moduleName) {
        for (TLAplusModuleDefinition moduleDef : getModuleDefinitionList()) {
            if (findPublic(moduleDef, moduleDef.getNonfixLhs().getNonfixLhsName(), moduleName)) {
                TLAplusModuleRef resolvedModuleRef = moduleDef.getInstance().getModuleRef();
                if (resolvedModuleRef != null) {
                    TLAplusModule module = findModule(resolvedModuleRef.getReferenceName());
                    if (module != null) {
                        return module;
                    }
                }
            }
        }
        // Find exported definitions by EXTENDS
        for (TLAplusModuleRef extend : getModuleRefList()) {
            String name = extend.getReferenceName();
            TLAplusModule module = findModule(name);
            if (module != null) {
                TLAplusModule resolvedModule = module.resolveModulePublic(moduleName);
                if (resolvedModule != null) {
                    return resolvedModule;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable TLAplusModule findModule(String moduleName) {
        PsiFile thisFile = getContainingFile();
        if (thisFile == null) {
            return null;
        }
        PsiDirectory directory = thisFile.getContainingDirectory();
        if (directory == null) {
            return null;
        }
        PsiFile moduleFile = directory.findFile(moduleName + ".tla");
        if (moduleFile == null) {
            return null;
        }
        return PsiTreeUtil.findChildOfType(moduleFile, TLAplusModule.class);
    }

    private static boolean findPublic(
            PsiElement maybeLocalDefinition,
            TLAplusNamedElement name,
            String referenceName) {
        if (maybeLocalDefinition.getPrevSibling() != null &&
            PsiUtilCore.getElementType(maybeLocalDefinition.getPrevSibling()) == TLAplusElementTypes.KEYWORD_LOCAL) {
            return false;
        }
        return Objects.equals(name.getName(), referenceName);
    }
}
