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

    private static final TextAttributesKey STRING = createAttrKey(
            "String", DefaultLanguageHighlighterColors.STRING);
    private static final TextAttributesKey NUMBER = createAttrKey(
            "Number", DefaultLanguageHighlighterColors.NUMBER);
    private static final TextAttributesKey KEYWORD = createAttrKey(
            "Keyword", DefaultLanguageHighlighterColors.KEYWORD);
    private static final TextAttributesKey BLOCK_COMMENT = createAttrKey(
            "BlockComment", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    private static final TextAttributesKey LINE_COMMENT = createAttrKey(
            "LineComment", DefaultLanguageHighlighterColors.LINE_COMMENT);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new TLAplusHighlightingLexer();
    }

    public static class TLAplusHighlightingLexer extends LayeredLexer {
        public TLAplusHighlightingLexer() {
            super(new TLAplusLexer());

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
        if (TokenSets.KEYWORDS.contains(tokenType)) {
            return pack(KEYWORD);
        }
        return TextAttributesKey.EMPTY_ARRAY;
    }
}
