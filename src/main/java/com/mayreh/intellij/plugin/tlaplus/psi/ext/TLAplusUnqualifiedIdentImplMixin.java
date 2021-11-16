package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusBoundName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusChooseExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusConstantDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusLetExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpArgName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusQuantifierExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusUnqualifiedIdent;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusVariableDecl;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusVariableName;

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
        public PsiElement handleElementRename(@NotNull String newElementName)
                throws IncorrectOperationException {
            return getElement();
        }

        @Override
        public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
            if (getElement().getReferenceName() == null) {
                return ResolveResult.EMPTY_ARRAY;
            }
            List<PsiElement> elements = new ArrayList<>();
            PsiElement context = getElement().getContext();

            while (context != null) {
                if (context instanceof TLAplusModule &&
                    process(getElement(), (TLAplusModule) context, elements)) {
                    break;
                }
                if (context instanceof TLAplusOpDefinition &&
                    process(getElement(), (TLAplusOpDefinition) context, elements)) {
                    break;
                }
                if (context instanceof TLAplusFuncDefinition &&
                    process(getElement(), (TLAplusFuncDefinition) context, elements)) {
                    break;
                }
                if (context instanceof TLAplusQuantifierExpr &&
                    process(getElement(), (TLAplusQuantifierExpr) context, elements)) {
                    break;
                }
                if (context instanceof TLAplusChooseExpr &&
                    process(getElement(), (TLAplusChooseExpr) context, elements)) {
                    break;
                }
                if (context instanceof TLAplusLetExpr &&
                    process(getElement(), (TLAplusLetExpr) context, elements)) {
                    break;
                }

                context = context.getContext();
            }
            return elements
                    .stream()
                    // TLA+ doesn't allow forward reference
                    .filter(e -> e.getTextOffset() <= getElement().getTextOffset())
                    .map(PsiElementResolveResult::new)
                    .toArray(ResolveResult[]::new);
        }

        private static boolean process(
                TLAplusUnqualifiedIdent ident,
                TLAplusModule context,
                List<PsiElement> elements) {
            for (TLAplusVariableDecl decl : context.getVariableDeclList()) {
                for (TLAplusVariableName name : decl.getVariableNameList()) {
                    if (Objects.equals(ident.getReferenceName(), name.getName())) {
                        elements.add(name);
                        return true;
                    }
                }
            }
            for (TLAplusOpDefinition opDef : context.getOpDefinitionList()) {
                if (opDef.getNonfixLhs() != null) {
                    if (Objects.equals(ident.getReferenceName(), opDef.getNonfixLhs().getNonfixLhsName().getName())) {
                        elements.add(opDef.getNonfixLhs().getNonfixLhsName());
                        return true;
                    }
                }
            }
            for (TLAplusFuncDefinition funcDef : context.getFuncDefinitionList()) {
                if (Objects.equals(ident.getReferenceName(), funcDef.getFuncName().getName())) {
                    elements.add(funcDef.getFuncName());
                    return true;
                }
            }
            for (TLAplusConstantDecl decl : context.getConstantDeclList()) {
                for (TLAplusOpDecl opDecl : decl.getOpDeclList()) {
                    if (opDecl.getOpName() != null &&
                        Objects.equals(ident.getReferenceName(), opDecl.getOpName().getName())) {
                        elements.add(opDecl.getOpName());
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean process(
                TLAplusUnqualifiedIdent ident,
                TLAplusOpDefinition context,
                List<PsiElement> elements) {
            if (context.getNonfixLhs() != null) {
                for (TLAplusOpArgName argName : context.getNonfixLhs().getOpArgNameList()) {
                    if (Objects.equals(ident.getReferenceName(), argName.getName())) {
                        elements.add(argName);
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean process(
                TLAplusUnqualifiedIdent ident,
                TLAplusFuncDefinition context,
                List<PsiElement> elements) {
            for (TLAplusBoundName boundName : context.getBoundNameList()) {
                if (Objects.equals(ident.getReferenceName(), boundName.getName())) {
                    elements.add(boundName);
                    return true;
                }
            }
            return false;
        }

        private static boolean process(
                TLAplusUnqualifiedIdent ident,
                TLAplusQuantifierExpr context,
                List<PsiElement> elements) {
            for (TLAplusBoundName boundName : context.getBoundNameList()) {
                if (Objects.equals(ident.getReferenceName(), boundName.getName())) {
                    elements.add(boundName);
                    return true;
                }
            }
            return false;
        }

        private static boolean process(
                TLAplusUnqualifiedIdent ident,
                TLAplusChooseExpr context,
                List<PsiElement> elements) {
            for (TLAplusBoundName boundName : context.getBoundNameList()) {
                if (Objects.equals(ident.getReferenceName(), boundName.getName())) {
                    elements.add(boundName);
                    return true;
                }
            }
            return false;
        }

        private static boolean process(
                TLAplusUnqualifiedIdent ident,
                TLAplusLetExpr context,
                List<PsiElement> elements) {
            for (TLAplusOpDefinition opDef : context.getOpDefinitionList()) {
                if (opDef.getNonfixLhs() != null) {
                    if (Objects.equals(ident.getReferenceName(), opDef.getNonfixLhs().getNonfixLhsName().getName())) {
                        elements.add(opDef.getNonfixLhs().getNonfixLhsName());
                        return true;
                    }
                }
            }
            for (TLAplusFuncDefinition funcDef : context.getFuncDefinitionList()) {
                if (Objects.equals(ident.getReferenceName(), funcDef.getFuncName().getName())) {
                    elements.add(funcDef.getFuncName());
                    return true;
                }
            }
            return false;
        }
    }
}
