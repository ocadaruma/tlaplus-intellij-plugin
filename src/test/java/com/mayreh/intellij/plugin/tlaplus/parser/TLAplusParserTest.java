package com.mayreh.intellij.plugin.tlaplus.parser;

import com.mayreh.intellij.plugin.ParserTestCaseBase;
import com.mayreh.intellij.plugin.tlaplus.TLAplusParserDefinition;

public class TLAplusParserTest extends ParserTestCaseBase {
    public TLAplusParserTest() {
        super("tlaplus/parser/fixtures", "tla", new TLAplusParserDefinition());
    }

    public void test_minimal() {
        doTest();
    }

    public void test_Junction() {
        doTest();
    }

    public void test_Junction2() {
        doTest();
    }

    public void test_fairness() {
        doTest();
    }

    public void test_ABSpec() {
        doTest();
    }

    public void test_TrickyLand() {
        doTest();
    }

    public void test_InfixPrefixOpMixed() {
        doTest();
    }

    public void test_OpDef() {
        doTest();
    }

    public void test_Precedence() {
        doTest();
    }

    public void test_union() {
        doTest();
    }

    public void test_NestedPrefixOpApply() {
        doTest();
    }

    public void test_recursive() {
        doTest();
    }

    public void test_case() {
        doTest();
    }

    public void test_theorem_assumption() {
        doTest();
    }
}
