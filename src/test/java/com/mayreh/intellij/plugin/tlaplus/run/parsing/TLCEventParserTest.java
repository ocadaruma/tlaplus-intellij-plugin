package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Test;

import com.mayreh.intellij.plugin.tlaplus.TestUtils;

public class TLCEventParserTest {
    @Test
    public void test() {
        doTest("PaxosCommit_success");
    }

    private static void doTest(String fileName) {
        List<TLCEvent> events = new ArrayList<>();
        Consumer<String> parserOuter = new Consumer<>() {
            private TLCEventParser parser = TLCEventParser.create(events::add);
            @Override
            public void accept(String s) {
                parser = parser.addLine(s);
            }
        };

        String output = TestUtils.resourceToString("tlc/fixtures/" + fileName + ".out");
        output.lines().forEach(parserOuter);
        String expected = TestUtils.resourceToString("tlc/fixtures/" + fileName + ".txt").trim();
        assertEquals(expected, events.stream().map(TLCEvent::toString).collect(Collectors.joining("\n")));
    }
}
