package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusChooseExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusLetExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusQuantifierExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusSetComprehension;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusSetComprehensionMap;

/**
 * Just a placeholder for now.
 */
public abstract class TLAplusElementImpl extends ASTWrapperPsiElement implements TLAplusElement {
    protected TLAplusElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public PsiElement getContext() {
        // In TLA+, there are 8 types of scopes (contexts).
        // 1. Module (e.g. variable_decl, constant_decl,...)
        // 2. Operator definition
        // 3. Function definition
        // 4. Quantifier expression
        // 5. Choose expression
        // 6. LET expression
        // 7. SET comprehension (e.g. { x : x \in some_set /\ x /= 1 })
        // 8. map-style SET comprehension (e.g. { x + 1 : x \in some_set })
        // We define "context" of a PsiElement as its nearest enclosing parent of above types.

        PsiElement element = getParent();
        while (element != null) {
            if (element instanceof TLAplusModule ||
                element instanceof TLAplusOpDefinition ||
                element instanceof TLAplusFuncDefinition ||
                element instanceof TLAplusQuantifierExpr ||
                element instanceof TLAplusChooseExpr ||
                element instanceof TLAplusLetExpr ||
                element instanceof TLAplusSetComprehension ||
                element instanceof TLAplusSetComprehensionMap) {
                return element;
            }

            element = element.getParent();
        }

        return null;
    }
}
