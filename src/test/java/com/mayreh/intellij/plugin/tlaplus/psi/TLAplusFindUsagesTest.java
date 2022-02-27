package com.mayreh.intellij.plugin.tlaplus.psi;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usageView.UsageInfo;

public class TLAplusFindUsagesTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testDashdotOp() {
        Collection<UsageInfo> usages = getUsagesAtCaret("DashdotOp.tla");
        Assert.assertEquals(1, usages.size());
    }

    private Collection<UsageInfo> getUsagesAtCaret(String... fileNames) {
        return myFixture.testFindUsages(
                Arrays.stream(fileNames)
                      .map(name -> "tlaplus/psi/findusages/fixtures/" + name)
                      .toArray(String[]::new));
    }
}
