package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

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
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
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
        TLAplusNamedElement result = find
                ((container, name) -> isLocalDefinitionOf(name, element, true));
        if (result != null) {
            return result;
        }
        result = findFromExtends(module -> module.findPublicDefinition(element.getReferenceName()));
        if (result != null) {
            return result;
        }
        // Definitions in the module instantiated by INSTANCE only visible
        // after INSTANCE declaration.
        result = findFromInstantiation(instance -> instance.getTextOffset() <= element.getTextOffset(),
                                       module -> module.findPublicDefinition(element.getReferenceName()));

        return result;
    }

    @Override
    public @Nullable TLAplusNamedElement findPublicDefinition(String referenceName) {
        TLAplusNamedElement result = find
                ((container, name) -> isPublicDefinitionOf(container, name, referenceName));
        if (result != null) {
            return result;
        }
        result = findFromExtends(module -> module.findPublicDefinition(referenceName));
        if (result != null) {
            return result;
        }
        result = findFromInstantiation(instance -> !isLocal(instance),
                                       module -> module.findPublicDefinition(referenceName));

        return result;
    }

    @Override
    public @Nullable TLAplusModule resolveModulePublic(String moduleName) {
        for (TLAplusModuleDefinition moduleDef : getModuleDefinitionList()) {
            if (isPublicDefinitionOf(moduleDef, moduleDef.getNonfixLhs().getNonfixLhsName(), moduleName)) {
                TLAplusModuleRef resolvedModuleRef = moduleDef.getInstance().getModuleRef();
                if (resolvedModuleRef != null) {
                    TLAplusModule module = findModule(resolvedModuleRef.getReferenceName());
                    if (module != null) {
                        return module;
                    }
                }
            }
        }

        TLAplusModule result = findFromExtends(module -> module.resolveModulePublic(moduleName));
        if (result != null) {
            return result;
        }

        result = findFromInstantiation(instance -> !isLocal(instance),
                                       module -> module.resolveModulePublic(moduleName));

        return result;
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

    private @Nullable TLAplusNamedElement find(BiPredicate<TLAplusElement, TLAplusNamedElement> predicate) {
        for (TLAplusVariableDecl decl : getVariableDeclList()) {
            for (TLAplusVariableName variableName : decl.getVariableNameList()) {
                if (predicate.test(decl, variableName)) {
                    return variableName;
                }
            }
        }
        for (TLAplusConstantDecl decl : getConstantDeclList()) {
            for (TLAplusOpDecl opDecl : decl.getOpDeclList()) {
                if (opDecl.getOpName() != null) {
                    if (predicate.test(decl, opDecl.getOpName())) {
                        return opDecl.getOpName();
                    }
                }
            }
        }
        for (TLAplusOpDefinition opDef : getOpDefinitionList()) {
            if (opDef.getNonfixLhs() != null) {
                if (predicate.test(opDef, opDef.getNonfixLhs().getNonfixLhsName())) {
                    return opDef.getNonfixLhs().getNonfixLhsName();
                }
            }
        }
        for (TLAplusFuncDefinition funcDef : getFuncDefinitionList()) {
            if (predicate.test(funcDef, funcDef.getFuncName())) {
                return funcDef.getFuncName();
            }
        }
        for (TLAplusModuleDefinition moduleDef : getModuleDefinitionList()) {
            if (predicate.test(moduleDef, moduleDef.getNonfixLhs().getNonfixLhsName())) {
                return moduleDef.getNonfixLhs().getNonfixLhsName();
            }
        }

        return null;
    }

    @Override
    public @Nullable <T> T findFromExtends(Function<TLAplusModule, @Nullable T> finder) {
        for (TLAplusModuleRef extend : getModuleRefList()) {
            String moduleName = extend.getReferenceName();
            TLAplusModule module = findModule(moduleName);
            if (module != null) {
                T result = finder.apply(module);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable <T> T findFromInstantiation(
            Predicate<TLAplusInstance> requirement,
            Function<TLAplusModule, @Nullable T> finder) {
        for (TLAplusInstance instance : getInstanceList()) {
            if (requirement.test(instance) && instance.getModuleRef() != null) {
                TLAplusModule module = findModule(instance.getModuleRef().getReferenceName());
                if (module != null) {
                    T result = finder.apply(module);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isPublicDefinitionOf(
            PsiElement container,
            TLAplusNamedElement name,
            String referenceName) {
        if (isLocal(container)) {
            return false;
        }
        return Objects.equals(name.getName(), referenceName);
    }

    private static boolean isLocal(PsiElement maybeLocalDefinition) {
        PsiElement sibling = PsiTreeUtil.skipWhitespacesAndCommentsBackward(maybeLocalDefinition);
        return sibling != null && PsiUtilCore.getElementType(sibling) == TLAplusElementTypes.KEYWORD_LOCAL;
    }
}
