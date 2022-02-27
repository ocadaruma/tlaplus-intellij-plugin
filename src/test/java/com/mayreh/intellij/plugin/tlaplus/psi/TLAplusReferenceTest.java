package com.mayreh.intellij.plugin.tlaplus.psi;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class TLAplusReferenceTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testBasic() {
        PsiReference reference = getReferenceAtCaret("Local.tla");

        TLAplusNonfixLhsName name = assertInstanceOf(reference.resolve(), TLAplusNonfixLhsName.class);
        Assert.assertEquals("Foo", name.getName());
    }

    public void testExtends() {
        PsiReference reference = getReferenceAtCaret("Extends_A.tla", "Extends_B.tla");

        TLAplusNonfixLhsName name = assertInstanceOf(reference.resolve(), TLAplusNonfixLhsName.class);
        Assert.assertEquals("Foo", name.getName());
        Assert.assertEquals("Extends_B", name.currentModule().getModuleHeader().getName());
    }

    public void testExtendsLocal() {
        PsiReference reference = getReferenceAtCaret("Extends_Local_A.tla", "Extends_Local_B.tla");
        // LOCAL definition should not be visible
        Assert.assertNull(reference.resolve());
    }

    public void testInstancePrefix() {
        PsiReference reference = getReferenceAtCaret(
                "InstancePrefix_A.tla",
                "InstancePrefix_B.tla",
                "InstancePrefix_C.tla");

        TLAplusNonfixLhsName name = assertInstanceOf(reference.resolve(), TLAplusNonfixLhsName.class);
        Assert.assertEquals("Foo", name.getName());
        Assert.assertEquals("InstancePrefix_C", name.currentModule().getModuleHeader().getName());
    }

    /*
     * Though cyclic module reference isn't allowed in TLA+ (SANY will report an error),
     * we should confirm that reference is resolved properly without causing stack overflow.
     */
    public void testCyclicModuleReference() {
        PsiReference reference = getReferenceAtCaret("Cycle_A.tla", "Cycle_B.tla", "Cycle_C.tla");

        TLAplusModuleHeader name = assertInstanceOf(reference.resolve(), TLAplusModuleHeader.class);
        Assert.assertEquals("Cycle_B", name.getName());
    }

    public void testExtendsCyclicModule() {
        PsiReference reference = getReferenceAtCaret("Cycle_0.tla", "Cycle_A.tla", "Cycle_B.tla", "Cycle_C.tla");

        TLAplusModuleHeader name = assertInstanceOf(reference.resolve(), TLAplusModuleHeader.class);
        Assert.assertEquals("Cycle_A", name.getName());
    }

    public void testFunction() {
        PsiReference reference = getReferenceAtCaret("Function.tla");

        TLAplusBoundName name = assertInstanceOf(reference.resolve(), TLAplusBoundName.class);
        Assert.assertEquals("bar", name.getName());
    }

    public void testCompletionStandardModules() {
        List<String> elements = getLookupElementStringsAtCaret("StandardModules.tla");
        Assert.assertNotNull(elements);
        assertSameElements(elements,
                           "Bags", "FiniteSets", "Integers", "Json",
                           "Naturals", "Randomization", "Reals", "RealTime",
                           "Sequences", "TLC", "TLCExt", "Toolbox");
    }

    public void testDashdotOp() {
        PsiReference reference = getReferenceAtCaret("DashdotOp.tla");

        TLAplusDashdotOpName name = assertInstanceOf(reference.resolve(), TLAplusDashdotOpName.class);
        Assert.assertEquals("-", name.getName());
    }

    public void testInfixOp() {
        PsiReference reference = getReferenceAtCaret("InfixOp.tla");

        TLAplusInfixOpName name = assertInstanceOf(reference.resolve(), TLAplusInfixOpName.class);
        Assert.assertEquals("+", name.getName());
    }

    public void testPostfixOp() {
        PsiReference reference = getReferenceAtCaret("PostfixOp.tla");

        TLAplusPostfixOpName name = assertInstanceOf(reference.resolve(), TLAplusPostfixOpName.class);
        Assert.assertEquals("^+", name.getName());
    }

    private PsiReference getReferenceAtCaret(String... fileNames) {
        return myFixture.getReferenceAtCaretPositionWithAssertion(
                Arrays.stream(fileNames)
                      .map(name -> "tlaplus/psi/reference/fixtures/" + name)
                      .toArray(String[]::new)
        );
    }

    private @Nullable List<String> getLookupElementStringsAtCaret(String... fileNames) {
        myFixture.configureByFiles(
                Arrays.stream(fileNames)
                      .map(name -> "tlaplus/psi/completion/fixtures/" + name)
                      .toArray(String[]::new));
        myFixture.complete(CompletionType.BASIC);
        return myFixture.getLookupElementStrings();
    }
}
