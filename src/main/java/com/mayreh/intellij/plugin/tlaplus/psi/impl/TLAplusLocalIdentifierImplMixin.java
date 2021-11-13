package com.mayreh.intellij.plugin.tlaplus.psi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.util.IncorrectOperationException;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusLocalIdentifier;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public abstract class TLAplusLocalIdentifierImplMixin
        extends ASTWrapperPsiElement implements TLAplusLocalIdentifier {
    public TLAplusLocalIdentifierImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new TLAplusLocalIdentifierReference(this);
    }

    @Override
    public @Nullable PsiElement getReferenceNameElement() {
        return findChildByType(TLAplusElementTypes.IDENTIFIER);
    }

    public static class TLAplusLocalIdentifierReference extends PsiPolyVariantReferenceBase<TLAplusLocalIdentifier> {
        public TLAplusLocalIdentifierReference(@NotNull TLAplusLocalIdentifier element) {
            super(element, new TextRange(0, element.getTextLength()));
        }

        @Override
        public PsiElement handleElementRename(@NotNull String newElementName)
                throws IncorrectOperationException {
            return getElement();
        }

//        @Override
//        public @Nullable PsiElement resolve() {
//            if (getElement().getReferenceName() == null) {
//                return null;
//            }
//            List<PsiElement> elements = new ArrayList<>();
//            PsiSearchHelper.getInstance(getElement().getProject())
//                           .processElementsWithWord(
//                                   (e, ignore) -> {
//                                       if (e instanceof TLAplusNamedElement) {
//                                           String name = ((TLAplusNamedElement) e).getName();
//                                           if (Objects.equals(name, getElement().getReferenceName())) {
//                                               elements.add(e);
//                                               return false;
//                                           }
//                                       }
//                                       return true;
//                                   },
//                                   GlobalSearchScope.fileScope(getElement().getContainingFile()),
//                                   getElement().getReferenceName(),
//                                   UsageSearchContext.IN_CODE,
//                                   false);
//            if (elements.isEmpty()) {
//                return null;
//            }
//            return elements.get(0);
//        }

        @Override
        public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
            if (getElement().getReferenceName() == null) {
                return ResolveResult.EMPTY_ARRAY;
            }
            List<PsiElement> elements = new ArrayList<>();
            PsiSearchHelper.getInstance(getElement().getProject())
                           .processElementsWithWord(
                                   (e, ignore) -> {
                                       if (e instanceof TLAplusNamedElement) {
                                           String name = ((TLAplusNamedElement) e).getName();
                                           if (Objects.equals(name, getElement().getReferenceName())) {
                                               elements.add(e);
                                               return false;
                                           }
                                       }
                                       return true;
                                   },
                                   GlobalSearchScope.fileScope(getElement().getContainingFile()),
                                   getElement().getReferenceName(),
                                   UsageSearchContext.IN_CODE,
                                   false);
//            if (elements.isEmpty()) {
//                return null;
//            }
            return elements
                    .stream()
                    .map(PsiElementResolveResult::new)
                    .toArray(ResolveResult[]::new);
        }
    }
}
