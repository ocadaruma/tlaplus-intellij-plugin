package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.mayreh.intellij.plugin.tlaplus.TestUtils;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.FunctionValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.PrimitiveValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.RecordValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.SetValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.TraceVariable;

public class TLCErrorTraceParserTest {
    @Test
    public void test() {
        Optional<ErrorTrace> result = TLCErrorTraceParser.parse(
                TestUtils.resourceToString("tlc/fixtures/errortrace/basic.out"));
        assertEquals(new ErrorTrace(asList(
                new TraceVariable("msgs", new SetValue(emptyList())),
                new TraceVariable("rmState", new FunctionValue(asList(
                        new FunctionValue.Entry("r1", new PrimitiveValue("\"working\"")),
                        new FunctionValue.Entry("r2", new PrimitiveValue("\"working\"")))
                )),
                new TraceVariable("aState", new FunctionValue(asList(
                        new FunctionValue.Entry("r1", new FunctionValue(asList(
                                new FunctionValue.Entry("a1", new RecordValue(asList(
                                        new RecordValue.Entry("bal", new PrimitiveValue("-1")),
                                        new RecordValue.Entry("mbal", new PrimitiveValue("0")),
                                        new RecordValue.Entry("val", new PrimitiveValue("\"none\""))
                                ))),
                                new FunctionValue.Entry("a2", new RecordValue(asList(
                                        new RecordValue.Entry("bal", new PrimitiveValue("-1")),
                                        new RecordValue.Entry("mbal", new PrimitiveValue("0")),
                                        new RecordValue.Entry("val", new PrimitiveValue("\"none\""))
                                ))),
                                new FunctionValue.Entry("a3", new RecordValue(asList(
                                        new RecordValue.Entry("bal", new PrimitiveValue("-1")),
                                        new RecordValue.Entry("mbal", new PrimitiveValue("0")),
                                        new RecordValue.Entry("val", new PrimitiveValue("\"none\""))
                                )))
                        ))),
                        new FunctionValue.Entry("r2", new FunctionValue(asList(
                                new FunctionValue.Entry("a1", new RecordValue(asList(
                                        new RecordValue.Entry("bal", new PrimitiveValue("-1")),
                                        new RecordValue.Entry("mbal", new PrimitiveValue("0")),
                                        new RecordValue.Entry("val", new PrimitiveValue("\"none\""))
                                ))),
                                new FunctionValue.Entry("a2", new RecordValue(asList(
                                        new RecordValue.Entry("bal", new PrimitiveValue("-1")),
                                        new RecordValue.Entry("mbal", new PrimitiveValue("0")),
                                        new RecordValue.Entry("val", new PrimitiveValue("\"none\""))
                                ))),
                                new FunctionValue.Entry("a3", new RecordValue(asList(
                                        new RecordValue.Entry("bal", new PrimitiveValue("-1")),
                                        new RecordValue.Entry("mbal", new PrimitiveValue("0")),
                                        new RecordValue.Entry("val", new PrimitiveValue("\"none\""))
                                )))
                        )))
                )))
        )), result.get());
    }
}
