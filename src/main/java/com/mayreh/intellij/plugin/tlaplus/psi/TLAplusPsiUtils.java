package com.mayreh.intellij.plugin.tlaplus.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.mayreh.intellij.plugin.tlaplus.psi.ext.TLAplusReferenceElement;

public class TLAplusPsiUtils {
    public static boolean isForwardReference(@NotNull TLAplusReferenceElement reference,
                                             @NotNull TLAplusNamedElement name) {
        return  reference.getTextOffset() < name.getTextOffset();
    }

    public static boolean isLocal(PsiElement maybeLocalDefinition) {
        PsiElement sibling = PsiTreeUtil.skipWhitespacesAndCommentsBackward(maybeLocalDefinition);
        return sibling != null && PsiUtilCore.getElementType(sibling) == TLAplusElementTypes.KEYWORD_LOCAL;
    }
}
