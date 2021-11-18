package com.mayreh.intellij.plugin.tlc.parser;

import com.mayreh.intellij.plugin.ParserTestCaseBase;
import com.mayreh.intellij.plugin.tlc.TLCConfigParserDefinition;

public class TLCConfigParserTest extends ParserTestCaseBase {
    public TLCConfigParserTest() {
        super("tlc/parser/fixtures", "cfg", new TLCConfigParserDefinition());
    }

    public void test_basic() {
        doTest();
    }
}
