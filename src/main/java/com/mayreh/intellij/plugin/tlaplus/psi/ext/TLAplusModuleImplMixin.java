package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isLocal;

import java.net.URL;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ResourceUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNonfixLhsName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusVariableName;

public abstract class TLAplusModuleImplMixin extends TLAplusElementImpl implements TLAplusModule {
    private static final Set<String> STANDARD_MODULES = Set.of(
            "Bags", "FiniteSets", "Integers", "Json", "Naturals", "Randomization",
            "Reals", "RealTime", "Sequences", "TLC", "TLCExt", "Toolbox");

    protected TLAplusModuleImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<? extends TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusReferenceElement reference) {
        Stream<? extends TLAplusNamedElement> localNames = definitions(
                (container, name) -> !isForwardReference(reference, name));

        Stream<TLAplusNamedElement> namesFromExtends = modulesFromExtends()
                .flatMap(TLAplusModuleContext::publicDefinitions);

        Stream<TLAplusNamedElement> namesFromInstance =
                // Definitions in the module instantiated by INSTANCE only visible
                // after INSTANCE declaration.
                modulesFromInstantiation(i -> i.getTextOffset() <= reference.getTextOffset())
                        .flatMap(TLAplusModuleContext::publicDefinitions);

        return Stream.concat(Stream.concat(localNames, namesFromExtends), namesFromInstance);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> publicDefinitions() {
        Stream<? extends TLAplusNamedElement> names = definitions(
                (container, name) -> !isLocal(container));

        Stream<TLAplusNamedElement> namesFromExtends = modulesFromExtends()
                .flatMap(TLAplusModuleContext::publicDefinitions);

        Stream<TLAplusNamedElement> namesFromInstance =
                modulesFromInstantiation(i -> !isLocal(i)).flatMap(TLAplusModuleContext::publicDefinitions);

        return Stream.concat(Stream.concat(names, namesFromExtends), namesFromInstance);
    }

    @Override
    public @Nullable TLAplusModule findModule(String moduleName) {
        TLAplusModule module = findStandardModule(moduleName);
        if (module != null) {
            return module;
        }

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

    private @NotNull Stream<? extends TLAplusNamedElement> definitions(
            BiPredicate<TLAplusElement, TLAplusNamedElement> requirement) {
        Stream<TLAplusVariableName> variables = getVariableDeclList()
                .stream()
                .flatMap(decl -> decl.getVariableNameList()
                                     .stream()
                                     .filter(name -> requirement.test(decl, name)));

        Stream<TLAplusOpName> constants = getConstantDeclList()
                .stream()
                .flatMap(decl -> decl.getOpDeclList()
                                     .stream()
                                     .map(TLAplusOpDecl::getOpName)
                                     .filter(name -> requirement.test(decl, name)));

        Stream<TLAplusNonfixLhsName> ops = getOpDefinitionList()
                .stream()
                .filter(def -> def.getNonfixLhs() != null && requirement.test(def, def.getNonfixLhs().getNonfixLhsName()))
                .map(def -> def.getNonfixLhs().getNonfixLhsName());

        Stream<TLAplusFuncName> functions = getFuncDefinitionList()
                .stream()
                .filter(def -> requirement.test(def, def.getFuncName()))
                .map(TLAplusFuncDefinition::getFuncName);

        Stream<TLAplusNonfixLhsName> modules = getModuleDefinitionList()
                .stream()
                .filter(def -> requirement.test(def, def.getNonfixLhs().getNonfixLhsName()))
                .map(def -> def.getNonfixLhs().getNonfixLhsName());

        return Stream.concat(Stream.concat(Stream.concat(Stream.concat(variables, constants), ops), functions), modules);
    }

    @Override
    public @NotNull Stream<TLAplusModule> modulesFromExtends() {
        return getModuleRefList()
                .stream()
                .map(TLAplusReferenceElement::getReferenceName)
                .map(this::findModule)
                .filter(Objects::nonNull);
    }

    @Override
    public @NotNull Stream<TLAplusModule> modulesFromInstantiation(
            Predicate<TLAplusInstance> requirement) {
        return getInstanceList()
                .stream()
                .filter(i -> requirement.test(i) && i.getModuleRef() != null)
                .map(i -> i.getModuleRef().getReferenceName())
                .map(this::findModule)
                .filter(Objects::nonNull);
    }

    private @Nullable TLAplusModule findStandardModule(String moduleName) {
        if (STANDARD_MODULES.contains(moduleName)) {
            URL url = ResourceUtil.getResource(getClass().getClassLoader(),
                                               "tla2sany/StandardModules",
                                               moduleName + ".tla");
            if (url == null) {
                return null;
            }

            VirtualFile file = VfsUtil.findFileByURL(url);
            if (file == null) {
                return null;
            }

            PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
            if (psiFile == null) {
                return null;
            }

            return PsiTreeUtil.findChildOfType(psiFile, TLAplusModule.class);
        }
        return null;
    }
}
