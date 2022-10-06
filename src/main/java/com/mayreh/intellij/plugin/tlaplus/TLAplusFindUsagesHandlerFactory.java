package com.mayreh.intellij.plugin.tlaplus;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.lang.findUsages.LanguageFindUsages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusSynonymElement;

public class TLAplusFindUsagesHandlerFactory extends FindUsagesHandlerFactory {
    @Override
    public boolean canFindUsages(@NotNull PsiElement element) {
        return TLAplusFindUsagesProvider.canFindUsages(element);
    }

    @Override
    public @Nullable FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element,
                                                               boolean forHighlightUsages) {
        return new TLAplusFindUsagesHandler(element);
    }

    private static class TLAplusFindUsagesHandler extends FindUsagesHandler {
        TLAplusFindUsagesHandler(@NotNull PsiElement psiElement) {
            super(psiElement);
        }

        @Override
        public PsiElement @NotNull [] getSecondaryElements() {
            if (myPsiElement instanceof TLAplusNamedElement) {
                TLAplusNamedElement element = (TLAplusNamedElement) myPsiElement;
                String[] synonyms = element.synonyms();
                if (synonyms != null) {
                    return Arrays.stream(synonyms)
                                 .filter(s -> !s.equals(element.getText().trim()))
                                 .map(s -> new TLAplusSynonymElement(element, s))
                                 .toArray(PsiElement[]::new);
                }
            }
            return super.getSecondaryElements();
        }
    }
}
