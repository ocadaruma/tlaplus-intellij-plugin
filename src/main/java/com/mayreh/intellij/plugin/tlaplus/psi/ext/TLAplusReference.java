package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isLocal;

import java.util.List;
import java.util.Objects;
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
                TLAplusModule module = currentModule.findModule(instance.getModuleRef().getReferenceName());
                if (module != null) {
                    return module.publicDefinitions()
                                 .map(PsiNamedElement::getName)
                                 .toArray();
                }
            }
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
                TLAplusModule module = currentModule.findModule(instance.getModuleRef().getReferenceName());
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
            TLAplusModule resolvedModule = currentModule.findModule(getElement().getReferenceName());
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
                module = moduleScope.findModule(moduleRef.getReferenceName());
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
            Predicate<TLAplusNamedElement> requirement,
            TLAplusModule currentModule,
            TLAplusModuleRef moduleRef) {
        Stream.Builder<Stream<TLAplusModule>> streams = Stream.builder();

        streams.add(
                localVariants(moduleRef)
                        .filter(requirement)
                        .flatMap(e -> {
                            if (e.getParent() == null) {
                                return Stream.empty();
                            }
                            if (!(e.getParent().getParent() instanceof TLAplusModuleDefinition)) {
                                return Stream.empty();
                            }
                            TLAplusModuleDefinition moduleDef = (TLAplusModuleDefinition) e.getParent().getParent();
                            return Optional.ofNullable(moduleDef.getInstance().getModuleRef())
                                           .flatMap(ref -> Optional.ofNullable(
                                                   currentModule.findModule(ref.getReferenceName())))
                                           .stream();
                        }));

        streams.add(currentModule.modulesFromExtends()
                                 .flatMap(m -> publicModuleVariants(m, requirement))
                                 .filter(m -> requirement.test(m.getModuleHeader())));

        streams.add(currentModule.modulesFromInstantiation(
                                         instance -> instance.getTextOffset() <= moduleRef.getTextOffset())
                                 .flatMap(m -> publicModuleVariants(m, requirement))
                                 .filter(m -> requirement.test(m.getModuleHeader())));

        return streams.build().flatMap(Function.identity());
    }

    private static @NotNull Stream<TLAplusNamedElement> localVariants(TLAplusReferenceElement element) {
        TLAplusNameContext context = null;
        if (element.getContext() instanceof TLAplusNameContext) {
            context = (TLAplusNameContext) element.getContext();
        }

        Stream.Builder<Stream<TLAplusNamedElement>> streams = Stream.builder();
        while (context != null) {
            streams.add(context.localDefinitions(element));

            if (context.getContext() instanceof TLAplusNameContext) {
                context = (TLAplusNameContext) context.getContext();
            } else {
                context = null;
            }
        }
        return streams.build().flatMap(Function.identity());
    }

    private static @NotNull Stream<TLAplusModule> publicModuleVariants(
            TLAplusModule context,
            Predicate<TLAplusNamedElement> requirement) {
        Stream.Builder<Stream<TLAplusModule>> streams = Stream.builder();

        streams.add(context.getModuleDefinitionList()
                           .stream()
                           .flatMap(def -> {
                               if (isLocal(def)) {
                                   return Stream.empty();
                               }
                               if (def.getInstance().getModuleRef() == null) {
                                   return Stream.empty();
                               }
                               if (!requirement.test(def.getNonfixLhs().getNonfixLhsName())) {
                                   return Stream.empty();
                               }
                               if (def.getInstance().getModuleRef() == null) {
                                   return Stream.empty();
                               }
                               return Optional.ofNullable(context.findModule(
                                       def.getInstance().getModuleRef().getReferenceName()))
                                       .stream();
                           }));

        streams.add(context.getModuleDefinitionList()
                           .stream()
                           .filter(def -> !isLocal(def))
                           .map(def -> def.getInstance().getModuleRef())
                           .filter(Objects::nonNull)
                           .map(ref -> context.findModule(ref.getReferenceName()))
                           .filter(Objects::nonNull));

        streams.add(context.modulesFromExtends()
                           .filter(m -> requirement.test(m.getModuleHeader()))
                           .flatMap(m -> publicModuleVariants(m, requirement)));
        streams.add(context.modulesFromInstantiation(instance -> !isLocal(instance))
                           .filter(m -> requirement.test(m.getModuleHeader()))
                           .flatMap(m -> publicModuleVariants(m, requirement)));

        return streams.build().flatMap(Function.identity());
    }
}
