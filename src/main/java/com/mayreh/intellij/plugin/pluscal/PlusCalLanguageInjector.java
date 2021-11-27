package com.mayreh.intellij.plugin.pluscal;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.psi.util.PsiUtilCore;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class PlusCalLanguageInjector implements MultiHostInjector {
    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!(context instanceof PsiCommentImpl) ||
            PsiUtilCore.getElementType(context) != TLAplusElementTypes.COMMENT_PLUS_CAL) {
            return;
        }

        if (StringUtil.isEmpty(context.getText())) {
            return;
        }

        registrar.startInjecting(PlusCalLanguage.INSTANCE);
        registrar.addPlace(
                null,
                null,
                (PsiLanguageInjectionHost) context,
                new TextRange(0, context.getTextLength()));
        registrar.doneInjecting();
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(PsiCommentImpl.class);
    }
}
