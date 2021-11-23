package com.mayreh.intellij.plugin.tlaplus.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.mayreh.intellij.plugin.tlaplus.psi.ext.TLAplusElement;

public class TLAplusPsiUtils {
    public static boolean isForwardReference(@NotNull TLAplusElement placement,
                                             @NotNull TLAplusNamedElement name) {
        return placement.getTextOffset() < name.getTextOffset();
    }

    public static boolean isLocal(TLAplusElement maybeLocalDefinition) {
        PsiElement sibling = PsiTreeUtil.skipWhitespacesAndCommentsBackward(maybeLocalDefinition);
        return sibling != null && PsiUtilCore.getElementType(sibling) == TLAplusElementTypes.KEYWORD_LOCAL;
    }
}
