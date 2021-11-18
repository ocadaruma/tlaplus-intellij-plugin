package com.mayreh.intellij.plugin;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LexerTestCase;

public abstract class LexerTestCaseBase extends LexerTestCase {
    private final String testDirResourcePath;
    private final String extension;

    protected LexerTestCaseBase(String testDirResourcePath, String extension) {
        this.testDirResourcePath = testDirResourcePath;
        this.extension = extension;
    }

    @Override
    protected @NotNull String getPathToTestDataFile(String extension) {
        return String.format("src/test/resources/%s/%s%s",
                             testDirResourcePath,
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
        doFileTest(extension);
    }
}
