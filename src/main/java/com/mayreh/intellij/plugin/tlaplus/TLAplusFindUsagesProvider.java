package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusLexer;
import com.mayreh.intellij.plugin.tlaplus.lexer.TokenSets;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public class TLAplusFindUsagesProvider implements FindUsagesProvider {
    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new TLAplusWordScanner();
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof TLAplusNamedElement;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull PsiElement element) {
        return "";
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        String name = "";
        if (element instanceof TLAplusNamedElement) {
            name = StringUtil.defaultIfEmpty(((TLAplusNamedElement) element).getName(), "");
        }
        return name;
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return "";
    }

    static class TLAplusWordScanner extends DefaultWordsScanner {
        TLAplusWordScanner() {
            super(new TLAplusLexer(false),
                  TokenSet.create(TLAplusElementTypes.IDENTIFIER),
                  TLAplusParserDefinition.COMMENT_TOKENS,
                  TokenSet.EMPTY,
                  TokenSet.EMPTY,
                  // We need to specify processAsWordTokenSet so that symbolic operators can be lexed correctly
                  TokenSets.OPERATORS);
        }
    }
}
