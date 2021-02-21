package com.mayreh.intellij.plugin.tlaplus.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlaplus.TestUtils;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

class TLAplusLexerTest {
    @Test
    void testMinimal() throws Exception {
        List<IElementType> tokens = tokenize(TestUtils.resourceToString("lexer/minimal.tla"));

        List<IElementType> expected = Arrays.asList(
                TLAplusElementTypes.IGNORED,
                TLAplusElementTypes.MODULE_BEGIN,
                TokenType.WHITE_SPACE,
                TLAplusElementTypes.IDENTIFIER,
                TokenType.WHITE_SPACE,
                TLAplusElementTypes.SEPARATOR,
                TokenType.WHITE_SPACE,
                TLAplusElementTypes.MODULE_END,
                TLAplusElementTypes.IGNORED
        );

        assertThat(tokens).isEqualTo(expected);
    }

    @Test
    void testNested() throws Exception {
        List<IElementType> tokens = tokenize(TestUtils.resourceToString("lexer/nested.tla"));

        List<IElementType> expected = Arrays.asList(
                TLAplusElementTypes.IGNORED,
                TLAplusElementTypes.MODULE_BEGIN,
                TokenType.WHITE_SPACE,
                TLAplusElementTypes.IDENTIFIER,
                TokenType.WHITE_SPACE,
                TLAplusElementTypes.SEPARATOR,
                TokenType.WHITE_SPACE,

                TLAplusElementTypes.MODULE_BEGIN,
                TokenType.WHITE_SPACE,
                TLAplusElementTypes.IDENTIFIER,
                TokenType.WHITE_SPACE,
                TLAplusElementTypes.SEPARATOR,
                TokenType.WHITE_SPACE,

                TLAplusElementTypes.MODULE_END,

                TokenType.WHITE_SPACE,
                TLAplusElementTypes.MODULE_END,

                TLAplusElementTypes.IGNORED
        );

        assertThat(tokens).isEqualTo(expected);
    }

    private static List<IElementType> tokenize(String text) {
        TLAplusLexer lexer = new TLAplusLexer();
        lexer.start(text);
        List<IElementType> tokens = new ArrayList<>();

        IElementType token;
        while ((token = lexer.getTokenType()) != null) {
            tokens.add(token);
            lexer.advance();
        }

        return tokens;
    }
}
