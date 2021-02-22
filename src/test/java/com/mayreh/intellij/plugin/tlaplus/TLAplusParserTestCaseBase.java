package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.ParsingTestCase;

public abstract class TLAplusParserTestCaseBase extends ParsingTestCase {
    protected TLAplusParserTestCaseBase() {
        super("parser/fixtures", "tla", new TLAplusParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    @Override
    protected @NotNull String getTestName(boolean lowercaseFirstLetter) {
        return StringUtil.trimStart(getName(), "test_");
    }

    protected void doTest() {
        doTest(true);
    }
}
