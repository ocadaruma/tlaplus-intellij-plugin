package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.VersionedWordsScanner;
import com.intellij.lang.cacheBuilder.WordOccurrence;
import com.intellij.lang.cacheBuilder.WordOccurrence.Kind;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.search.PsiSearchHelperImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusLexer;
import com.mayreh.intellij.plugin.tlaplus.lexer.TokenSets;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public class TLAplusFindUsagesProvider implements FindUsagesProvider {
    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new TLAplusWordsScanner();
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

    /**
     * {@link WordsScanner} impl that scans operators properly.
     *
     * {@link DefaultWordsScanner} is not suitable here because:
     * - DefaultWordsScanner works like:
     *   * For identifierTokenSet, strip non-identifier characters then scan them
     *   * For processAsWordTokenSet, scan them without strip
     *
     * Let's say how it works if we use it to scan OP_LTEQ token.
     * OP_LTEQ has 3 synonyms that \leq, =<, <= .
     *
     * If we specify OP_LTEQ as identifierTokenSet, since =<, <= consist of only non-identifier character,
     * it can't be indexed correctly.
     * On the other hand, if we specify OP_LTEQ as processAsWordTokenSet, \leq will be indexed as \leq (as-is).
     * However, this also be a problem because {@link PsiSearchHelperImpl} has a logic to strip non-identifier characters by its own
     * (in getWordEntries), hence, actual search-word will be `leq`, which is inconsistent with indexed word (\leq).
     *
     * To address it, we implement custom scanner to scan words as similar logic as {@link PsiSearchHelperImpl}#getWordEntries.
     * That is:
     * - Strip non-identifier characters from the word
     * - If it's non-empty, just scan it. If it's empty (i.e. the word consist only of non-identifier characters), index it without strip.
     */
    private static class TLAplusWordsScanner extends VersionedWordsScanner {
        private final TLAplusLexer lexer = new TLAplusLexer(false);

        @Override
        public void processWords(@NotNull CharSequence fileText, @NotNull Processor<? super WordOccurrence> processor) {
            lexer.start(fileText);
            WordOccurrence occurrence = new WordOccurrence(fileText, 0, 0, null);

            IElementType type;
            while ((type = lexer.getTokenType()) != null) {
                if (type == TLAplusElementTypes.IDENTIFIER || TokenSets.OPERATORS.contains(type)) {
                    if (!stripWord(processor, occurrence, fileText, lexer.getTokenStart(), lexer.getTokenEnd())) {
                        return;
                    }
                }
                lexer.advance();
            }
        }

        private static boolean stripWord(
                @NotNull Processor<? super WordOccurrence> processor,
                @NotNull WordOccurrence occurrence,
                @NotNull CharSequence text,
                int from,
                int to) {
            int index = from;

            while (true) {
                if (index == to) {
                    break;
                }
                char c = text.charAt(index);
                if (isAsciiIdentifierPart(c)) {
                    break;
                }
                index++;
            }

            if (index == to) {
                occurrence.init(text, from, to, Kind.CODE);
                return processor.process(occurrence);
            }
            if (index < to) {
                occurrence.init(text, index, to, Kind.CODE);
                return processor.process(occurrence);
            }

            return true;
        }

        private static boolean isAsciiIdentifierPart(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
        }
    }
}
