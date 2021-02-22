package com.mayreh.intellij.plugin.tlaplus;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LexerTestCase;

public abstract class TLAplusLexerTestCaseBase extends LexerTestCase {

    protected abstract String resourcePrefix();

    @Override
    protected String getDirPath() {
        throw new UnsupportedOperationException();
    }

    protected void doTest() {
        String testName = StringUtil.trimStart(getName(), "test_");

        String text = TestUtils.resourceToString(
                resourcePrefix() + '/' + testName + ".tla");
        String expected = TestUtils.resourceToString(
                resourcePrefix() + '/' + testName + ".txt");
        doTest(text, expected, createLexer());
    }
}
