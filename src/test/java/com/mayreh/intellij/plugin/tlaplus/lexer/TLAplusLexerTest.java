package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.LexerTestCaseBase;

public class TLAplusLexerTest extends LexerTestCaseBase {
    public TLAplusLexerTest() {
        super("tlaplus/lexer/fixtures", "tla");
    }

    @Override
    protected Lexer createLexer() {
        return new TLAplusLexer(false);
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

    public void test_Junction2() {
        doTest();
    }

    public void test_Junction3() {
        doTest();
    }

    public void test_Junction4() {
        doTest();
    }

    public void test_fairness() {
        doTest();
    }

    public void test_PlusCalCSyntaxComment() {
        doTest();
    }
}
