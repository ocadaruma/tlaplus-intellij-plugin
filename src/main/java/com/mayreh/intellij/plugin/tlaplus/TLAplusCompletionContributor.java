package com.mayreh.intellij.plugin.tlaplus;

import static com.intellij.patterns.PlatformPatterns.*;
import static com.mayreh.intellij.plugin.tlaplus.TLAplusCompletionContributor.Patterns.tlaplusElement;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern.Capture;
import com.intellij.patterns.TreeElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ProcessingContext;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModuleHeader;

public class TLAplusCompletionContributor extends CompletionContributor implements DumbAware {
    public TLAplusCompletionContributor() {
        extend(CompletionType.BASIC,
               psiElement().withParent(psiElement(TLAplusModule.class)),
               new KeywordCompletionProvider("LOCAL", "VARIABLE", "VARIABLES", "CONSTANT", "CONSTANTS", "INSTANCE", "THEOREM", "ASSUME", "ASSUMPTION", "AXIOM"));
        extend(CompletionType.BASIC,
               tlaplusElement().afterSibling2(psiElement(TLAplusModuleHeader.class)),
               new KeywordCompletionProvider("EXTENDS"));
    }

    private static class KeywordCompletionProvider extends CompletionProvider<CompletionParameters> {
        private final String[] keywords;
        KeywordCompletionProvider(String... keywords) {
            this.keywords = keywords;
        }

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            for (String keyword : keywords) {
                result.caseInsensitive().addElement(LookupElementBuilder.create(keyword).bold());
            }
        }
    }

    static class Patterns<T extends PsiElement> extends Capture<T> {
        private static final TokenSet SKIP_TOKENS = TokenSet.create(
                TLAplusElementTypes.COMMENT_LINE,
                TLAplusElementTypes.COMMENT_BLOCK,
                TLAplusElementTypes.COMMENT_PLUS_CAL,
                TLAplusElementTypes.IGNORED,
                TokenType.WHITE_SPACE);

        protected Patterns(Class<T> aClass) {
            super(aClass);
        }

        /**
         * Unlike {@link TreeElementPattern#afterSibling}, finds matching patterns by just
         * going backwards siblings.
         * {@link TreeElementPattern#afterSibling} finds parent's children, however, at this moment,
         * parent may not include the element as its children while element's parent (thus its sibling) is already set
         * (not sure why......), so it may not work.
         */
        public Capture<T> afterSibling2(ElementPattern<? extends PsiElement> pattern) {
            return with(new PatternCondition<T>("afterSibling2") {
                @Override
                public boolean accepts(@NotNull T t, ProcessingContext context) {
                    PsiElement sibling = t.getPrevSibling();
                    while (sibling != null) {
                        if (!SKIP_TOKENS.contains(PsiUtilCore.getElementType(sibling))) {
                            return pattern.accepts(sibling);
                        }
                        sibling = sibling.getPrevSibling();
                    }
                    return false;
                }
            });
        }

        public static Patterns<PsiElement> tlaplusElement() {
            return new Patterns<>(PsiElement.class);
        }
    }
}
