package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isLocal;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtilRt;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusGeneralIdentifier;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstancePrefix;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleRef;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiFactory;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusSubstitutingIdent;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusUnqualifiedIdent;

public class TLAplusReference<T extends TLAplusReferenceElement> extends PsiReferenceBase<T> {
    public TLAplusReference(@NotNull T element) {
        super(element, new TextRange(0, element.getTextLength()));
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        PsiElement newIdent = new TLAplusPsiFactory(getElement().getProject()).createIdentifier(newElementName);
        getElement().getIdentifier().replace(newIdent);
        return getElement();
    }

    // TODO: Change icons by modules/constants/variables/... etc
    @Override
    public Object @NotNull [] getVariants() {
        TLAplusModule currentModule = getElement().currentModule();
        if (currentModule == null) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }

        if (getElement() instanceof TLAplusUnqualifiedIdent) {
            return unqualifiedIdentVariants(currentModule, (TLAplusUnqualifiedIdent) getElement())
                    .map(PsiNamedElement::getName)
                    .toArray();
        }

        if (getElement() instanceof TLAplusSubstitutingIdent) {
            TLAplusSubstitutingIdent ident = (TLAplusSubstitutingIdent) getElement();
            TLAplusInstance instance = PsiTreeUtil.getParentOfType(ident, TLAplusInstance.class);
            if (instance != null && instance.getModuleRef() != null) {
                TLAplusModule module = currentModule
                        .availableModules()
                        .filter(m -> instance.getModuleRef().getReferenceName().equals(m.getModuleHeader().getName()))
                        .findFirst()
                        .orElse(null);
                if (module != null) {
                    return module.publicDefinitions()
                                 .map(PsiNamedElement::getName)
                                 .toArray();
                }
            }
        }

        if (getElement() instanceof TLAplusModuleRef) {
            return currentModule.availableModules()
                                .flatMap(m -> Optional.ofNullable(m.getModuleHeader().getName()).stream())
                                .toArray();
        }

        return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public @Nullable PsiElement resolve() {
        TLAplusModule currentModule = getElement().currentModule();
        if (currentModule == null) {
            return null;
        }

        if (getElement() instanceof TLAplusUnqualifiedIdent) {
            return unqualifiedIdentVariants(currentModule, (TLAplusUnqualifiedIdent) getElement())
                    .filter(name -> getElement().getReferenceName().equals(name.getName()))
                    .findFirst()
                    .orElse(null);
        }

        if (getElement() instanceof TLAplusSubstitutingIdent) {
            TLAplusSubstitutingIdent ident = (TLAplusSubstitutingIdent) getElement();
            TLAplusInstance instance = PsiTreeUtil.getParentOfType(ident, TLAplusInstance.class);
            if (instance != null && instance.getModuleRef() != null) {
                TLAplusModule module = currentModule
                        .availableModules()
                        .filter(m -> instance.getModuleRef().getReferenceName().equals(m.getModuleHeader().getName()))
                        .findFirst()
                        .orElse(null);
                if (module != null) {
                    return module.publicDefinitions()
                                 .filter(name -> ident.getReferenceName().equals(name.getName()))
                                 .findFirst()
                                 .orElse(null);
                }
            }
        }

        if (getElement() instanceof TLAplusModuleRef) {
            // If the module is a plain module (i.e. without instantiation), just resolved to its module header.
            TLAplusModule resolvedModule = currentModule
                    .availableModules()
                    .filter(m -> getElement().getReferenceName().equals(m.getModuleHeader().getName()))
                    .findFirst()
                    .orElse(null);
            if (resolvedModule != null) {
                return resolvedModule.getModuleHeader();
            }

            if (getElement().getParent() instanceof TLAplusInstancePrefix) {
                TLAplusInstancePrefix instancePrefix = (TLAplusInstancePrefix) getElement().getParent();
                int index = instancePrefix.getModuleRefList().indexOf(getElement());

                if (index == 0) {
                    // For first moduleReference, it should be resolved locally as like other element.
                    return localVariants(getElement())
                            .filter(name -> getElement().getReferenceName().equals(name.getName()))
                            .findFirst()
                            .orElse(null);
                }
                if (index > 0) {
                    // Otherwise, search scope should be narrowed first and should resolve in that scope.
                    TLAplusModule scope = resolveInstancePrefix(
                            currentModule, instancePrefix.getModuleRefList().subList(0, index));
                    if (scope != null) {
                        return scope.publicDefinitions()
                                    .filter(name -> getElement().getReferenceName().equals(name.getName()))
                                    .findFirst()
                                    .orElse(null);
                    }
                }
            } else {
                // If the module is not inside instance prefix, just resolve as like other element.
                return localVariants(getElement())
                        .filter(name -> getElement().getReferenceName().equals(name.getName()))
                        .findFirst()
                        .orElse(null);
            }
        }

        return null;
    }

    private static @NotNull Stream<TLAplusNamedElement> unqualifiedIdentVariants(
            TLAplusModule currentModule, TLAplusUnqualifiedIdent element) {
        TLAplusGeneralIdentifier generalIdentifier = null;
        if (element.getParent() instanceof TLAplusGeneralIdentifier) {
            generalIdentifier = (TLAplusGeneralIdentifier) element.getParent();
        }

        if (generalIdentifier == null || generalIdentifier.getInstancePrefix() == null) {
            return localVariants(element);
        }

        TLAplusModule resolvedModule = resolveInstancePrefix(
                currentModule,
                generalIdentifier.getInstancePrefix().getModuleRefList());

        if (resolvedModule == null) {
            return Stream.empty();
        }

        return resolvedModule.publicDefinitions();
    }

    private static @Nullable TLAplusModule resolveInstancePrefix(
            TLAplusModule currentModule, List<TLAplusModuleRef> moduleRefs) {
        if (moduleRefs.isEmpty()) {
            return null;
        }

        TLAplusModule moduleScope = currentModule;
        if (moduleScope == null) {
            return null;
        }
        for (int i = 0; i < moduleRefs.size(); i++) {
            TLAplusModuleRef moduleRef = moduleRefs.get(i);
            // We should lookup plain module (i.e. without instantiation) or find module definition locally first
            // without taking visibility into account.
            TLAplusModule module;
            if (i == 0) {
                module = moduleScope
                        .availableModules()
                        .filter(m -> moduleRef.getReferenceName().equals(m.getModuleHeader().getName()))
                        .findFirst()
                        .orElse(null);
                if (module != null) {
                    moduleScope = module;
                    continue;
                }
                module = localModuleVariants(name -> moduleRef.getReferenceName().equals(name.getName()),
                                             moduleScope,
                                             moduleRef)
                        .findFirst()
                        .orElse(null);
            } else {
                module = publicModuleVariants(moduleScope,
                                              name -> moduleRef.getReferenceName().equals(name.getName()))
                        .findFirst()
                        .orElse(null);
            }
            if (module != null) {
                moduleScope = module;
            } else {
                // If failed to lookup module, just returns immediately
                return null;
            }
        }
        return moduleScope;
    }

    private static @NotNull Stream<TLAplusModule> localModuleVariants(
            Predicate<TLAplusNamedElement> moduleRefNameFilter,
            TLAplusModule currentModule,
            TLAplusElement placement) {
        Stream.Builder<Stream<TLAplusModule>> streams = Stream.builder();

        streams.add(
                localVariants(placement)
                        .filter(moduleRefNameFilter)
                        .flatMap(e -> {
                            if (e.getParent() == null) {
                                return Stream.empty();
                            }
                            if (!(e.getParent().getParent() instanceof TLAplusModuleDefinition)) {
                                return Stream.empty();
                            }
                            TLAplusModuleDefinition moduleDef = (TLAplusModuleDefinition) e.getParent().getParent();
                            return Optional.ofNullable(moduleDef.getInstance().getModuleRef())
                                           .flatMap(ref -> currentModule.availableModules()
                                                                        .filter(m -> ref.getReferenceName().equals(
                                                                                m.getModuleHeader().getName()))
                                                                        .findFirst())
                                           .stream();
                        }));

        streams.add(currentModule.modulesFromExtends()
                                 .flatMap(m -> publicModuleVariants(m, moduleRefNameFilter))
                                 .filter(m -> moduleRefNameFilter.test(m.getModuleHeader())));

        streams.add(currentModule.modulesFromInstantiation(
                                         instance -> instance.getTextOffset() <= placement.getTextOffset())
                                 .flatMap(m -> publicModuleVariants(m, moduleRefNameFilter))
                                 .filter(m -> moduleRefNameFilter.test(m.getModuleHeader())));

        return streams.build().flatMap(Function.identity());
    }

    /**
     * Returns the stream of variants that are available at the placement.
     */
    private static @NotNull Stream<TLAplusNamedElement> localVariants(TLAplusElement placement) {
        TLAplusNameContext context = null;
        if (placement.getContext() instanceof TLAplusNameContext) {
            context = (TLAplusNameContext) placement.getContext();
        }

        Stream.Builder<Stream<TLAplusNamedElement>> streams = Stream.builder();
        while (context != null) {
            streams.add(context.localDefinitions(placement));

            if (context.getContext() instanceof TLAplusNameContext) {
                context = (TLAplusNameContext) context.getContext();
            } else {
                context = null;
            }
        }
        return streams.build().flatMap(Function.identity());
    }

    /**
     * Return the stream of modules which are visible in the context and meet specified moduleRefNameFilter.
     * moduleRefNameFilter applies to the reference name that the module is exposed as.
     * i.e. if we have module definition like `INSTANCE_X == INSTANCE X`, filter applies to `INSTANCE_X` (not X)
     */
    private static @NotNull Stream<TLAplusModule> publicModuleVariants(
            TLAplusModule context,
            Predicate<TLAplusNamedElement> moduleRefNameFilter) {
        Stream.Builder<Stream<TLAplusModule>> streams = Stream.builder();

        streams.add(context.getModuleDefinitionList()
                           .stream()
                           .flatMap(def -> {
                               if (isLocal(def)) {
                                   return Stream.empty();
                               }
                               if (!moduleRefNameFilter.test(def.getNonfixLhs().getNonfixLhsName())) {
                                   return Stream.empty();
                               }
                               if (def.getInstance().getModuleRef() == null) {
                                   return Stream.empty();
                               }
                               return context.availableModules()
                                             .filter(m -> def.getInstance().getModuleRef().getReferenceName()
                                                             .equals(m.getModuleHeader().getName()));
                           }));

        streams.add(context.modulesFromExtends()
                           .filter(m -> moduleRefNameFilter.test(m.getModuleHeader()))
                           .flatMap(m -> publicModuleVariants(m, moduleRefNameFilter)));
        streams.add(context.modulesFromInstantiation(instance -> !isLocal(instance))
                           .filter(m -> moduleRefNameFilter.test(m.getModuleHeader()))
                           .flatMap(m -> publicModuleVariants(m, moduleRefNameFilter)));

        return streams.build().flatMap(Function.identity());
    }
}
