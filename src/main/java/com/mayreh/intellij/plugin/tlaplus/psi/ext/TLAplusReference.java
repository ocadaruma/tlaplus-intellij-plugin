package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isLocal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
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

        List<Object> result = new ArrayList<>();

        result.add("test1");
        result.add("foooo");

        return result.toArray();
    }

    @Override
    public @Nullable PsiElement resolve() {
        TLAplusModule currentModule = getElement().currentModule();
        if (currentModule == null) {
            return null;
        }

        if (getElement() instanceof TLAplusUnqualifiedIdent) {
            return resolveUnqualifiedIdent(currentModule, (TLAplusUnqualifiedIdent) getElement());
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
                    return resolveReferenceLocally(getElement());
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
                return resolveReferenceLocally(getElement());
            }
        }

        return null;
    }

    private @Nullable PsiElement resolveUnqualifiedIdent(
            TLAplusModule currentModule, TLAplusUnqualifiedIdent element) {
        TLAplusGeneralIdentifier generalIdentifier = null;
        if (element.getParent() instanceof TLAplusGeneralIdentifier) {
            generalIdentifier = (TLAplusGeneralIdentifier) element.getParent();
        }

        if (generalIdentifier == null || generalIdentifier.getInstancePrefix() == null) {
            return resolveReferenceLocally(getElement());
        }

        TLAplusModule resolvedModule = resolveInstancePrefix(
                currentModule, generalIdentifier.getInstancePrefix().getModuleRefList());

        if (resolvedModule == null) {
            return null;
        }

        return resolvedModule.publicDefinitions()
                             .filter(name -> element.getReferenceName().equals(name.getName()))
                             .findFirst()
                             .orElse(null);
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
                module = resolveModuleLocally(moduleScope, moduleRef);
            } else {
                module = resolveModulePublic(moduleScope, moduleRef.getReferenceName());
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

    private static @Nullable TLAplusModule resolveModuleLocally(
            TLAplusModule currentModule, TLAplusModuleRef moduleRef) {
        TLAplusNamedElement moduleDefName = resolveReferenceLocally(moduleRef);
        if (moduleDefName != null) {
            PsiElement element = moduleDefName.getParent();
            if (element != null) {
                element = element.getParent();
            }
            if (element instanceof TLAplusModuleDefinition) {
                TLAplusModuleDefinition moduleDefinition = (TLAplusModuleDefinition) element;
                TLAplusModuleRef resolvedModuleRef = moduleDefinition.getInstance().getModuleRef();
                if (resolvedModuleRef != null) {
                    String resolvedModuleName = resolvedModuleRef.getReferenceName();
                    return currentModule.findModule(resolvedModuleName);
                }
            }
        }

        Optional<TLAplusModule> result = currentModule
                .modulesFromExtends()
                .map(m -> resolveModulePublic(m, moduleRef.getReferenceName()))
                .filter(Objects::nonNull)
                .findFirst();
        if (result.isPresent()) {
            return result.get();
        }

        result = currentModule
                .modulesFromInstantiation(
                        instance -> instance.getTextOffset() <= moduleRef.getTextOffset())
                .map(m -> resolveModulePublic(m, moduleRef.getReferenceName()))
                .filter(Objects::nonNull)
                .findFirst();

        return result.orElse(null);
    }

    private static @Nullable TLAplusNamedElement resolveReferenceLocally(TLAplusReferenceElement element) {
        TLAplusNameContext context = null;
        if (element.getContext() instanceof TLAplusNameContext) {
            context = (TLAplusNameContext) element.getContext();
        }
        while (context != null) {
            Optional<? extends TLAplusNamedElement> name =
                    context.localDefinitions(element)
                           .filter(e -> element.getReferenceName().equals(e.getName()))
                           .findFirst();
            if (name.isPresent()) {
                return name.get();
            }

            if (context.getContext() instanceof TLAplusNameContext) {
                context = (TLAplusNameContext) context.getContext();
            } else {
                context = null;
            }
        }
        return null;
    }

    private static @Nullable TLAplusModule resolveModulePublic(TLAplusModule context, String moduleName) {
        for (TLAplusModuleDefinition moduleDef : context.getModuleDefinitionList()) {
            if (!isLocal(moduleDef) && moduleName.equals(moduleDef.getNonfixLhs().getNonfixLhsName().getName())) {
                TLAplusModuleRef resolvedModuleRef = moduleDef.getInstance().getModuleRef();
                if (resolvedModuleRef != null) {
                    TLAplusModule module = context.findModule(resolvedModuleRef.getReferenceName());
                    if (module != null) {
                        return module;
                    }
                }
            }
        }

        Optional<TLAplusModule> result = context.modulesFromExtends()
               .map(m -> resolveModulePublic(m, moduleName))
               .filter(Objects::nonNull)
               .findFirst();
        if (result.isPresent()) {
            return result.get();
        }

        result = context.modulesFromInstantiation(instance -> !isLocal(instance))
                        .map(m -> resolveModulePublic(m, moduleName))
                        .filter(Objects::nonNull)
                        .findFirst();

        return result.orElse(null);
    }
}
