package com.mayreh.intellij.plugin.pluscal.lexer;

import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.LexerTestCaseBase;
import com.mayreh.intellij.plugin.pluscal.PlusCalLexer;

public class PlusCalLexerTest extends LexerTestCaseBase {
    public PlusCalLexerTest() {
        super("pluscal/lexer/fixtures", "tla");
    }

    @Override
    protected Lexer createLexer() {
        return new PlusCalLexer(false);
    }

    public void test_Euclid_C() {
        doTest();
    }

    public void test_Label_C() {
        doTest();
    }
}
