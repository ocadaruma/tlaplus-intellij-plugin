package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LexerTestCase;

public abstract class TLAplusLexerTestCaseBase extends LexerTestCase {
    @Override
    protected @NotNull String getPathToTestDataFile(String extension) {
        return String.format("src/test/resources/lexer/fixtures/%s%s",
                             getTestName(false), extension);
    }

    @Override
    protected String getDirPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected @NotNull String getTestName(boolean lowercaseFirstLetter) {
        return StringUtil.trimStart(getName(), "test_");
    }

    protected void doTest() {
        doFileTest("tla");
    }
}
