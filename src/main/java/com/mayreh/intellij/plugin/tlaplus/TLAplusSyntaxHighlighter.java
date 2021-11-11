package com.mayreh.intellij.plugin.tlaplus;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusLexer;
import com.mayreh.intellij.plugin.tlaplus.lexer.TokenSets;
import com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusModuleBeginLexer;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusSyntaxHighlighter extends SyntaxHighlighterBase {
    private static TextAttributesKey createAttrKey(String name, TextAttributesKey key) {
        return createTextAttributesKey("com.mayreh.tlaplus." + name, key);
    }

    public static final TextAttributesKey STRING = createAttrKey(
            "String", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER = createAttrKey(
            "Number", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey KEYWORD = createAttrKey(
            "Keyword", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BLOCK_COMMENT = createAttrKey(
            "BlockComment", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey LINE_COMMENT = createAttrKey(
            "LineComment", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey PARENTHESIS = createAttrKey(
            "Parenthesis", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACES = createAttrKey(
            "Braces", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS = createAttrKey(
            "Brackets", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey COMMA = createAttrKey(
            "Comma", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey OPERATOR = createAttrKey(
            "Operator", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new TLAplusHighlightingLexer();
    }

    public static class TLAplusHighlightingLexer extends LayeredLexer {
        public TLAplusHighlightingLexer() {
            super(new TLAplusLexer(true));

            registerLayer(new FlexAdapter(new _TLAplusModuleBeginLexer(null)),
                          TLAplusElementTypes.MODULE_BEGIN);
        }
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
        if (tokenType.equals(TLAplusElementTypes.LPAREN) || tokenType.equals(TLAplusElementTypes.RPAREN)) {
            return pack(PARENTHESIS);
        }
        if (tokenType.equals(TLAplusElementTypes.LBRACKET) || tokenType.equals(TLAplusElementTypes.RBRACKET)) {
            return pack(BRACES);
        }
        if (tokenType.equals(TLAplusElementTypes.LSQBRACKET) || tokenType.equals(TLAplusElementTypes.RSQBRACKET)) {
            return pack(BRACKETS);
        }
        if (tokenType.equals(TLAplusElementTypes.COMMA)) {
            return pack(COMMA);
        }
        if (TokenSets.OPERATORS.contains(tokenType)) {
            return pack(OPERATOR);
        }
        if (TokenSets.KEYWORDS.contains(tokenType)) {
            return pack(KEYWORD);
        }
        return TextAttributesKey.EMPTY_ARRAY;
    }
}
