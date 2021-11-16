package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiFactory;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusUnqualifiedIdent;

public abstract class TLAplusUnqualifiedIdentImplMixin extends TLAplusElementImpl implements TLAplusUnqualifiedIdent {
    protected TLAplusUnqualifiedIdentImplMixin(@NotNull ASTNode node) {
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

    public static class TLAplusLocalIdentifierReference extends PsiPolyVariantReferenceBase<TLAplusUnqualifiedIdent> {
        public TLAplusLocalIdentifierReference(@NotNull TLAplusUnqualifiedIdent element) {
            super(element, new TextRange(0, element.getTextLength()));
        }

        @Override
        public PsiElement handleElementRename(@NotNull String newElementName) {
            PsiElement newIdent = new TLAplusPsiFactory(getElement().getProject()).createIdentifier(newElementName);
            getElement().getIdentifier().replace(newIdent);
            return getElement();
        }

        @Override
        public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
            if (getElement().getReferenceName() == null) {
                return ResolveResult.EMPTY_ARRAY;
            }
            List<PsiElement> elements = new ArrayList<>();
            TLAplusNameContext context = null;
            if (getElement().getContext() instanceof TLAplusNameContext) {
                context = (TLAplusNameContext) getElement().getContext();
            }
            while (context != null) {
                TLAplusNamedElement definition = context.findDefinition(getElement());
                if (definition != null) {
                    elements.add(definition);
                    break;
                }

                if (context.getContext() instanceof TLAplusNameContext) {
                    context = (TLAplusNameContext) context.getContext();
                } else {
                    context = null;
                }
            }
            return elements
                    .stream()
                    .map(PsiElementResolveResult::new)
                    .toArray(ResolveResult[]::new);
        }
    }
}
