package com.mayreh.intellij.plugin.tlc;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusSyntaxHighlighter;
import com.mayreh.intellij.plugin.tlc.lexer.TLCConfigLexer;
import com.mayreh.intellij.plugin.tlc.psi.TLCConfigElementTypes;

public class TLCConfigSyntaxHighlighter extends SyntaxHighlighterBase {
    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new TLCConfigLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(TLCConfigElementTypes.LITERAL_STRING)) {
            return pack(TLAplusSyntaxHighlighter.STRING);
        }
        if (tokenType.equals(TLCConfigElementTypes.LITERAL_NUMBER)) {
            return pack(TLAplusSyntaxHighlighter.NUMBER);
        }
        if (tokenType.equals(TLCConfigElementTypes.COMMENT)) {
            return pack(TLAplusSyntaxHighlighter.BLOCK_COMMENT);
        }
        if (tokenType.equals(TLCConfigElementTypes.LBRACE) || tokenType.equals(TLCConfigElementTypes.RBRACE)) {
            return pack(TLAplusSyntaxHighlighter.BRACES);
        }
        if (tokenType.equals(TLCConfigElementTypes.COMMA)) {
            return pack(TLAplusSyntaxHighlighter.COMMA);
        }
        if (TokenSets.KEYWORDS.contains(tokenType)) {
            return pack(TLAplusSyntaxHighlighter.KEYWORD);
        }
        if (TokenSets.OPERATORS.contains(tokenType)) {
            return pack(TLAplusSyntaxHighlighter.OPERATOR);
        }
        return TextAttributesKey.EMPTY_ARRAY;
    }
}
