package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isLocal;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusDashdotOpName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInfixOpName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleRef;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPrefixOpName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiFactory;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusSubstitutingIdent;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusUnqualifiedInfixOp;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusUnqualifiedPrefixOp;

public class TLAplusReference extends PsiReferenceBase<TLAplusReferenceElement> {
    private final Predicate<TLAplusNamedElement> variantFilter;

    public TLAplusReference(@NotNull TLAplusReferenceElement element,
                            Predicate<TLAplusNamedElement> variantFilter) {
        super(element, new TextRange(0, element.getTextLength()));
        this.variantFilter = variantFilter;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        if (getElement() instanceof TLAplusIdentifierReferenceElement) {
            PsiElement identifier = ((TLAplusIdentifierReferenceElement) getElement()).getIdentifier();
            PsiElement newIdent = new TLAplusPsiFactory(getElement().getProject()).createIdentifier(newElementName);
            identifier.replace(newIdent);
            return getElement();
        }
        throw new IncorrectOperationException("Can't rename symbolic operator");
    }

    /**
     * Returns the stream of variants that are available at the placement.
     * This will return all variants without taking element type into account.
     * (e.g. When we have `INSTANCE M`, M's variants should be limited to only module names but
     * this method will return all variants including operator/constant etc.)
     * You have to filter out unnecessary variants by proper variantFilter.
     */
    private Stream<? extends TLAplusNamedElement> variants() {
        TLAplusModule currentModule = getElement().currentModule();
        if (currentModule == null) {
            return Stream.empty();
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
                    return module.publicDefinitions(new HashSet<>());
                }
            }
        }

        return identifierVariants(currentModule, getElement());
    }

    // TODO: Change icons by modules/constants/variables/... etc
    @Override
    public Object @NotNull [] getVariants() {
        return variants()
                .filter(variantFilter)
                .flatMap(e -> Optional.ofNullable(e.getName()).stream())
                .toArray();
    }

    @Override
    public @Nullable PsiElement resolve() {
        return variants()
                .filter(variantFilter)
                .filter(e -> getElement().getReferenceName().equals(e.getName()))
                .filter(e -> {
                    // `-` can be used as either prefix operator or infix operator.
                    // We need to filter by prefix/infix here so that can be resolved to correct operator definition.
                    if (getElement() instanceof TLAplusUnqualifiedInfixOp) {
                        return e instanceof TLAplusInfixOpName;
                    }
                    if (getElement() instanceof TLAplusUnqualifiedPrefixOp) {
                        return e instanceof TLAplusPrefixOpName || e instanceof TLAplusDashdotOpName;
                    }
                    return true;
                })
                .findFirst()
                .orElse(null);
    }

    private static @NotNull Stream<TLAplusNamedElement> identifierVariants(
            TLAplusModule currentModule, TLAplusElement element) {
        TLAplusGeneralReference generalReference = null;
        if (element.getParent() instanceof TLAplusGeneralReference) {
            generalReference = (TLAplusGeneralReference) element.getParent();
        }

        if (generalReference == null || generalReference.getInstancePrefix() == null) {
            return unqualifiedVariants(element);
        }

        TLAplusModule resolvedModule = resolveInstancePrefix(
                currentModule,
                generalReference.getInstancePrefix().getModuleRefList());

        if (resolvedModule == null) {
            return Stream.empty();
        }

        return resolvedModule.publicDefinitions(new HashSet<>());
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
                unqualifiedVariants(placement)
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
    private static @NotNull Stream<TLAplusNamedElement> unqualifiedVariants(TLAplusElement placement) {
        TLAplusNameContext context = null;
        if (placement.getContext() instanceof TLAplusNameContext) {
            context = (TLAplusNameContext) placement.getContext();
        }

        Stream.Builder<Stream<TLAplusNamedElement>> streams = Stream.builder();
        while (context != null) {
            // add all local names into the variants
            streams.add(context.localDefinitions(placement));
            // available module names are kind of "names" implicitly defined
            // by directory structure and standard modules.
            // so we add them into the variants as well.
            if (context instanceof TLAplusModuleContext) {
                streams.add(((TLAplusModuleContext) context)
                                    .availableModules()
                                    .map(TLAplusModule::getModuleHeader));
            }

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
