package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.mayreh.intellij.plugin.tlaplus.TestUtils;

public class TLCEventParserTest extends BasePlatformTestCase {

    public void testPaxosCommitSuccess() {
        doTest("PaxosCommit_success");
    }

    private void doTest(String fileName) {
        List<TLCEvent> events = new ArrayList<>();
        Consumer<String> parserOuter = new Consumer<>() {
            private TLCEventParser parser = TLCEventParser.create(events::add, getProject());
            @Override
            public void accept(String s) {
                parser = parser.addLine(s);
            }
        };

        String output = TestUtils.resourceToString("tlc/fixtures/" + fileName + ".out");
        output.lines().forEach(parserOuter);
        String expected = TestUtils.resourceToString("tlc/fixtures/" + fileName + ".txt").trim();
        Assert.assertEquals(expected, events.stream().map(TLCEvent::toString).collect(Collectors.joining("\n")));
    }
}
