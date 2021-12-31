package com.mayreh.intellij.plugin.tlc;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiComment;
import com.intellij.util.ProcessingContext;

public class TLCConfigCompletionContributor extends CompletionContributor implements DumbAware {
    public TLCConfigCompletionContributor() {
        extend(CompletionType.BASIC,
               psiElement().andNot(psiElement(PsiComment.class)),
               new KeywordCompletionProvider(
                       "CONSTANT", "CONSTANTS",
                       "CONSTRAINT", "CONSTRAINT",
                       "ACTION_CONSTRAINT", "ACTION_CONSTRAINT",
                       "INVARIANT", "INVARIANTS",
                       "INIT", "NEXT", "VIEW", "SYMMETRY", "SPECIFICATION",
                       "PROPERTY", "PROPERTIES",
                       "ALIAS", "POSTCONDITION", "CHECK_DEADLOCK"));
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
}
