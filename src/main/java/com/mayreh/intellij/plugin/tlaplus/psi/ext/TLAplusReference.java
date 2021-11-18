package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusGeneralIdentifier;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleHeader;
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
        TLAplusNameContext context = null;
        if (getElement().getContext() instanceof TLAplusNameContext) {
            context = (TLAplusNameContext) getElement().getContext();
        }
        while (context != null) {
            TLAplusNamedElement definition = context.findLocalDefinition(getElement());
            if (definition != null) {
                return definition;
            }

            if (context.getContext() instanceof TLAplusNameContext) {
                context = (TLAplusNameContext) context.getContext();
            } else {
                context = null;
            }
        }

//        if (getElement().getParent() instanceof TLAplusGeneralIdentifier) {
//            TLAplusGeneralIdentifier generalIdent = (TLAplusGeneralIdentifier) getElement().getParent();
//            if (generalIdent.getInstancePrefix() != null) {
//                generalIdent.getInstancePrefix().getModuleRefList().indexOf()
//            }
//        }
//
//        // find other files when no definition found so far
//        if (getElement() instanceof TLAplusModuleRef) {
//            PsiFile file = getElement().getContainingFile();
//            if (file != null && file.getContainingDirectory() != null) {
//                String moduleName = getElement().getReferenceName();
//                PsiFile moduleFile = file.getContainingDirectory().findFile(moduleName + ".tla");
//                return PsiTreeUtil.findChildOfType(moduleFile, TLAplusModuleHeader.class);
//            }
//        }
//
//        if (getElement() instanceof TLAplusUnqualifiedIdent) {
//            // unqualified_ident's parent is always general_identifier
//            TLAplusGeneralIdentifier generalIdentifier = (TLAplusGeneralIdentifier) getElement().getParent();
//            TLAplusModule moduleScope = PsiTreeUtil.getParentOfType(getElement(), TLAplusModule.class);
//            if (generalIdentifier.getInstancePrefix() != null) {
//                for (TLAplusModuleRef moduleRef : generalIdentifier.getInstancePrefix().getModuleRefList()) {
//                    moduleScope = resolveModule(moduleRef.getReferenceName(), moduleScope);
//                }
//                // If failed to identify module scope while instance prefix is specified, return immediately
//                // to avoid resolving to wrong element in current file
//                if (moduleScope == null) {
//                    return null;
//                }
//            }
//
//        }

        return null;
    }

//    private static @Nullable TLAplusModule resolveModule(
//            @NotNull String moduleRefName, @Nullable TLAplusModule scope) {
//        TLAplusModuleRef ref = moduleRef;
//        while (ref != null) {
//            // getReference() here always non-null
//            PsiElement resolved = ref.getReference().resolve();
//            if (resolved instanceof TLAplusModuleHeader) {
//                return (TLAplusModuleHeader) resolved;
//            }
//            TLAplusModuleDefinition moduleDef = PsiTreeUtil.getParentOfType(
//                    resolved, TLAplusModuleDefinition.class);
//            if (moduleDef != null) {
//                ref = moduleDef.getInstance().getModuleRef();
//            } else {
//                ref = null;
//            }
//        }
//        return null;
//    }
}
