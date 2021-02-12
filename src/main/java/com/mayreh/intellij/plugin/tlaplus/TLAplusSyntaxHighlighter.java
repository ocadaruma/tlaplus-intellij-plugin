package com.mayreh.intellij.plugin.tlaplus;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusLexer;
import com.mayreh.intellij.plugin.tlaplus.lexer.TokenSets;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey STRING = createTextAttributesKey(
            "String", DefaultLanguageHighlighterColors.STRING);
    private static final TextAttributesKey NUMBER = createTextAttributesKey(
            "Number", DefaultLanguageHighlighterColors.NUMBER);
    private static final TextAttributesKey KEYWORD = createTextAttributesKey(
            "Keyword", DefaultLanguageHighlighterColors.KEYWORD);
    private static final TextAttributesKey BLOCK_COMMENT = createTextAttributesKey(
            "BlockComment", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    private static final TextAttributesKey LINE_COMMENT = createTextAttributesKey(
            "LineComment", DefaultLanguageHighlighterColors.LINE_COMMENT);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new TLAplusLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(TLAplusElementTypes.LITERAL_STRING)) {
            return pack(STRING);
        }
        if (tokenType.equals(TLAplusElementTypes.LITERAL_NUMBER)) {
            return pack(NUMBER);
        }
        if (tokenType.equals(TLAplusElementTypes.COMMENT_BLOCK)) {
            return pack(BLOCK_COMMENT);
        }
        if (tokenType.equals(TLAplusElementTypes.IGNORED)) {
            return pack(BLOCK_COMMENT);
        }
        if (tokenType.equals(TLAplusElementTypes.COMMENT_LINE)) {
            return pack(LINE_COMMENT);
        }
        if (TokenSets.KEYWORDS.contains(tokenType)) {
            return pack(KEYWORD);
        }
        return TextAttributesKey.EMPTY_ARRAY;
    }
}
