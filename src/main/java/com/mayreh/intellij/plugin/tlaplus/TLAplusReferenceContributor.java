package com.mayreh.intellij.plugin.tlaplus;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.or;

import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.mayreh.intellij.plugin.tlaplus.psi.ext.TLAplusReference;
import com.mayreh.intellij.plugin.tlaplus.psi.ext.TLAplusReferenceElement;
import com.mayreh.intellij.plugin.tlc.psi.TLCConfigConstantReference;
import com.mayreh.intellij.plugin.tlc.psi.TLCConfigOperatorReference;

public class TLAplusReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                or(psiElement(TLCConfigOperatorReference.class),
                   psiElement(TLCConfigConstantReference.class)),
                new TLCConfigFileReferenceProvider());
    }

    private static class TLCConfigFileReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                               @NotNull ProcessingContext context) {
            if (element instanceof TLAplusReferenceElement) {
                return new PsiReference[]{ new TLAplusReference(
                        (TLAplusReferenceElement) element, e -> true) };
            }
            return PsiReference.EMPTY_ARRAY;
        }
    }
}
