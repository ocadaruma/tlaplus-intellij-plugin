package com.mayreh.intellij.plugin.pluscal;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PlatformPatterns.psiFile;

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

public class PlusCalCompletionContributor extends CompletionContributor implements DumbAware {
    public PlusCalCompletionContributor() {
        extend(CompletionType.BASIC,
               // TODO: more fine-grained completion control
               psiElement().inFile(psiFile().withLanguage(PlusCalLanguage.INSTANCE))
                           .andNot(psiElement(PsiComment.class)),
               new KeywordCompletionProvider("variable", "variables", "define", "macro", "procedure", "fair", "process", "begin", "end", "algorithm"));
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
