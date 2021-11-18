package com.mayreh.intellij.plugin.tlc.lexer;

import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.LexerTestCaseBase;

public class TLCConfigLexerTest extends LexerTestCaseBase {
    public TLCConfigLexerTest() {
        super("tlc/lexer/fixtures", "cfg");
    }

    @Override
    protected Lexer createLexer() {
        return new TLCConfigLexer();
    }

    public void test_basic() {
        doTest();
    }
}
