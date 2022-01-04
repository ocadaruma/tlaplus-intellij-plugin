package com.mayreh.intellij.plugin.util;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

public class TLAplusTreeUtil {
    public static <T extends PsiElement> Optional<T> findChildOfType(
            @Nullable PsiElement element,
            @NotNull Class<T> clazz) {
        return Optional.ofNullable(PsiTreeUtil.findChildOfType(element, clazz));
    }

    public static <T extends PsiElement> boolean hasParentOfType(
            @Nullable PsiElement element,
            @NotNull Class<T> clazz) {
        return PsiTreeUtil.getParentOfType(element, clazz) != null;
    }
}
