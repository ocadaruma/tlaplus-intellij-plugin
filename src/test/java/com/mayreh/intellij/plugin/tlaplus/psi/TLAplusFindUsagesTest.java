package com.mayreh.intellij.plugin.tlaplus.psi;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.intellij.psi.PsiElement;
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

    public void testSynonym() {
        Collection<UsageInfo> usages = getUsagesAtCaret("Synonym.tla");
        Assert.assertEquals(
                Set.of("\\leq", "=<", "<="),
                usages.stream()
                      .map(UsageInfo::getElement)
                      .filter(Objects::nonNull)
                      .map(PsiElement::getText)
                      .collect(toSet()));
    }

    private Collection<UsageInfo> getUsagesAtCaret(String... fileNames) {
        return myFixture.testFindUsages(
                Arrays.stream(fileNames)
                      .map(name -> "tlaplus/psi/findusages/fixtures/" + name)
                      .toArray(String[]::new));
    }
}
