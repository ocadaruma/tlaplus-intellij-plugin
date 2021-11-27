package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.LexerTestCaseBase;

public class TLAplusPlusCalCommentLexerTest extends LexerTestCaseBase {
    public TLAplusPlusCalCommentLexerTest() {
        super("tlaplus/lexer/fixtures", "tla");
    }

    @Override
    protected Lexer createLexer() {
        return new FlexAdapter(new _TLAplusPlusCalCommentLexer(null));
    }

    public void test_PlusCalComment() {
        doTest();
    }
}
