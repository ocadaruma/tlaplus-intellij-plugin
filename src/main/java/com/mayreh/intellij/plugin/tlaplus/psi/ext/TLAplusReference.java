package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusGeneralIdentifier;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstancePrefix;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleRef;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiFactory;
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
    public @Nullable PsiElement resolve() {
        TLAplusModule currentModule = PsiTreeUtil.getParentOfType(getElement(), TLAplusModule.class);
        if (currentModule == null) {
            return null;
        }

        if (getElement() instanceof TLAplusUnqualifiedIdent) {
            return resolveUnqualifiedIdent(currentModule, (TLAplusUnqualifiedIdent) getElement());
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
                        return scope.findPublicDefinition(getElement().getReferenceName());
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

        return resolvedModule.findPublicDefinition(element.getReferenceName());
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
                module = moduleScope.resolveModulePublic(moduleRef.getReferenceName());
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

        // Find exported definitions by EXTENDS
        for (TLAplusModuleRef extend : currentModule.getModuleRefList()) {
            String moduleName = extend.getReferenceName();
            TLAplusModule module = currentModule.findModule(moduleName);
            if (module != null) {
                TLAplusModule resolvedModule = module.resolveModulePublic(moduleRef.getReferenceName());
                if (resolvedModule != null) {
                    return resolvedModule;
                }
            }
        }

        return null;
    }

    private static @Nullable TLAplusNamedElement resolveReferenceLocally(TLAplusReferenceElement element) {
        TLAplusNameContext context = null;
        if (element.getContext() instanceof TLAplusNameContext) {
            context = (TLAplusNameContext) element.getContext();
        }
        while (context != null) {
            TLAplusNamedElement definition = context.findLocalDefinition(element);
            if (definition != null) {
                return definition;
            }

            if (context.getContext() instanceof TLAplusNameContext) {
                context = (TLAplusNameContext) context.getContext();
            } else {
                context = null;
            }
        }
        return null;
    }
}
