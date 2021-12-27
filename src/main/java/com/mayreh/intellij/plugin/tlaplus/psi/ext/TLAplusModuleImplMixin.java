package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isLocal;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ResourceUtil;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDecl;

public abstract class TLAplusModuleImplMixin extends TLAplusElementImpl implements TLAplusModule {
    private static final Set<String> STANDARD_MODULES = Set.of(
            "Bags", "FiniteSets", "Integers", "Json", "Naturals", "Randomization",
            "Reals", "RealTime", "Sequences", "TLC", "TLCExt", "Toolbox");

    protected TLAplusModuleImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusElement placement) {
        Stream.Builder<Stream<TLAplusNamedElement>> streams = Stream.builder();

        streams.add(definitions((container, name) -> !isForwardReference(placement, name)));
        streams.add(modulesFromExtends().flatMap(TLAplusModuleContext::publicDefinitions));
        // Definitions in the module instantiated by INSTANCE only visible
        // after INSTANCE declaration.
        streams.add(modulesFromInstantiation(i -> i.getTextOffset() <= placement.getTextOffset())
                            .flatMap(TLAplusModuleContext::publicDefinitions));

        return streams.build().flatMap(Function.identity());
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> publicDefinitions() {
        Stream.Builder<Stream<TLAplusNamedElement>> streams = Stream.builder();
        streams.add(definitions((container, name) -> !isLocal(container)));
        streams.add(modulesFromExtends().flatMap(TLAplusModuleContext::publicDefinitions));
        streams.add(modulesFromInstantiation(i -> !isLocal(i)).flatMap(TLAplusModuleContext::publicDefinitions));

        return streams.build().flatMap(Function.identity());
    }

    @Override
    public @NotNull Stream<TLAplusModule> availableModules() {
        Stream<TLAplusModule> standardModules = standardModules();
        Stream<TLAplusModule> modulesInSameDir =
                Optional.ofNullable(getContainingFile())
                        .stream()
                        .flatMap(file -> {
                            PsiDirectory directory = file.getOriginalFile().getContainingDirectory();
                            if (directory != null) {
                                return Stream.of(Pair.pair(file, directory));
                            }
                            if (getContainingFile() instanceof TLAplusFile) {
                                return Optional
                                        .ofNullable(((TLAplusFile) getContainingFile()).codeFragmentContext())
                                        .stream()
                                        .flatMap(ctx -> Optional
                                                .ofNullable(VfsUtil.findFile(ctx.directory(), true))
                                                .stream()
                                                .flatMap(dir -> Optional
                                                        .ofNullable(PsiManager.getInstance(getProject()).findDirectory(dir)).stream()))
                                        .map(dir -> Pair.pair(file, dir));
                            }
                            return Stream.empty();
                        })
                        .flatMap(pair -> Arrays
                                .stream(pair.second.getFiles())
                                // this file should not be included
                                .filter(f -> f.getName().endsWith(".tla") && !f.getName().equals(pair.first.getName()))
                                .flatMap(f -> Optional.ofNullable(
                                        // TODO: There are many codes that finding module in PsiFile
                                        PsiTreeUtil.findChildOfType(f, TLAplusModule.class)).stream()));

        return Stream.concat(standardModules, modulesInSameDir);
    }

    private @NotNull Stream<TLAplusNamedElement> definitions(
            BiPredicate<TLAplusElement, TLAplusNamedElement> requirement) {
        Stream.Builder<Stream<TLAplusNamedElement>> streams = Stream.builder();

        streams.add(getVariableDeclList()
                            .stream()
                            .flatMap(decl -> decl
                                    .getVariableNameList()
                                    .stream()
                                    .filter(name -> requirement.test(decl, name))));

        streams.add(getConstantDeclList()
                            .stream()
                            .flatMap(decl -> decl
                                    .getOpDeclList()
                                    .stream()
                                    .map(TLAplusOpDecl::getOpName)
                                    .filter(name -> requirement.test(decl, name))));

        streams.add(getOpDefinitionList()
                            .stream()
                            .filter(def -> def.getNonfixLhs() != null &&
                                           requirement.test(def, def.getNonfixLhs().getNonfixLhsName()))
                            .map(def -> def.getNonfixLhs().getNonfixLhsName()));

        streams.add(getFuncDefinitionList()
                            .stream()
                            .filter(def -> requirement.test(def, def.getFuncName()))
                            .map(TLAplusFuncDefinition::getFuncName));

        streams.add(getModuleDefinitionList()
                            .stream()
                            .filter(def -> requirement.test(def, def.getNonfixLhs().getNonfixLhsName()))
                            .map(def -> def.getNonfixLhs().getNonfixLhsName()));

        return streams.build().flatMap(Function.identity());
    }

    @Override
    public @NotNull Stream<TLAplusModule> modulesFromExtends() {
        return getModuleRefList()
                .stream()
                .map(TLAplusReferenceElement::getReferenceName)
                .flatMap(moduleName -> availableModules().filter(m -> moduleName.equals(m.getModuleHeader().getName())));
    }

    @Override
    public @NotNull Stream<TLAplusModule> modulesFromInstantiation(
            Predicate<TLAplusInstance> requirement) {
        return getInstanceList()
                .stream()
                .filter(i -> requirement.test(i) && i.getModuleRef() != null)
                .map(i -> i.getModuleRef().getReferenceName())
                .flatMap(moduleName -> availableModules().filter(m -> moduleName.equals(m.getModuleHeader().getName())));
    }

    private @NotNull Stream<TLAplusModule> standardModules() {
        return STANDARD_MODULES
                .stream()
                .flatMap(moduleName -> {
                    URL url = ResourceUtil.getResource(
                            getClass().getClassLoader(),
                            "tla2sany/StandardModules",
                            moduleName + ".tla");
                    if (url == null) {
                        return Stream.empty();
                    }
                    VirtualFile file = VfsUtil.findFileByURL(url);
                    if (file == null) {
                        return Stream.empty();
                    }
                    PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
                    if (psiFile == null) {
                        return Stream.empty();
                    }
                    return Optional.ofNullable(PsiTreeUtil.findChildOfType(psiFile, TLAplusModule.class))
                                   .stream();
                });
    }
}
