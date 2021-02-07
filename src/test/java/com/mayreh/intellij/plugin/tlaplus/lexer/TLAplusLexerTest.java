package com.mayreh.intellij.plugin.tlaplus.lexer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.intellij.lexer.FlexAdapter;
import com.intellij.psi.tree.IElementType;

class TLAplusLexerTest {
    @Test
    void testTokenize() throws Exception {
        FlexAdapter lexer = new FlexAdapter(new TLAplusLexer(null));
        lexer.start(IOUtils.resourceToString("/lexer/example.tla", StandardCharsets.UTF_8));
        List<IElementType> tokens = new ArrayList<>();
        IElementType token;

        while ((token = lexer.getTokenType()) != null) {
            tokens.add(token);
            System.out.println(token);
            lexer.advance();
        }
    }
}
