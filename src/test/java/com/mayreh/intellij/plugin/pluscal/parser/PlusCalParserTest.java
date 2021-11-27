package com.mayreh.intellij.plugin.pluscal.parser;

import com.mayreh.intellij.plugin.ParserTestCaseBase;
import com.mayreh.intellij.plugin.pluscal.PlusCalParserDefinition;
import com.mayreh.intellij.plugin.tlaplus.TLAplusParserDefinition;

public class PlusCalParserTest extends ParserTestCaseBase {
    public PlusCalParserTest() {
        super("pluscal/parser/fixtures",
              "tla",
              new PlusCalParserDefinition(),
              new TLAplusParserDefinition());
    }

    public void test_HyperEuclid_C() {
        doTest();
    }
}
