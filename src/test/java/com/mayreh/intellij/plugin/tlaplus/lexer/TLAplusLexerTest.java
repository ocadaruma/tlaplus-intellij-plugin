package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLexerTestCaseBase;

public class TLAplusLexerTest extends TLAplusLexerTestCaseBase {
    @Override
    protected Lexer createLexer() {
        return new TLAplusLexer();
    }

    public void test_minimal() {
        doTest();
    }

    public void test_nested() {
        doTest();
    }

    public void test_ABSpec() {
        doTest();
    }

    public void test_Junction() {
        doTest();
    }
}
