package com.mayreh.intellij.plugin;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.ParsingTestCase;

public abstract class ParserTestCaseBase extends ParsingTestCase {
    protected ParserTestCaseBase(String testDirResourcePath,
                                 String extension,
                                 ParserDefinition... parserDefinitions) {
        super(testDirResourcePath, extension, parserDefinitions);
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
