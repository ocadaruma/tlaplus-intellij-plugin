package com.mayreh.intellij.plugin.tlaplus.run.eval;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class ExpressionEvaluatorTest {
    @Test
    public void testEval() {
        assertEquals(
                new Result("{1, 2, 3}", Collections.emptyList()),
                ExpressionEvaluator.evaluate(null, "{1,2,3}"));
    }

    @Test
    public void testEvalInContext() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("tlaplus/run/eval").toURI());
        Context ctx = new Context("Foo", path);
        assertEquals(
                new Result("{9, 10, 11}", Collections.emptyList()),
                ExpressionEvaluator.evaluate(ctx, "Baz"));
    }

    @Test
    public void testInvalid() throws Exception {
        Result result = ExpressionEvaluator.evaluate(null, "XYZ");
        assertEquals(new Result("", Arrays.asList(
                "Parsing or semantic analysis failed.",
                "line 6, col 14 to line 6, col 16 of module __dummy_module__\n\nUnknown operator: `XYZ'."
        )), result);
    }

    @Test
    public void testInvalidContext() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("tlaplus/run/eval").toURI());
        Context ctx = new Context("Foo", path);
        Result result = ExpressionEvaluator.evaluate(ctx, "S");
        assertEquals(new Result("", Arrays.asList(
                "Evaluating an expression of the form Append(s, v) when s is not a sequence:\n"
                + "(1 :> \"x\" @@ 3 :> \"x\")"
        )), result);
    }

    @Test
    public void testPrint() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("tlaplus/run/eval").toURI());
        Context ctx = new Context("Foo2", path);
        Result result = ExpressionEvaluator.evaluate(ctx, "T");
        assertEquals(new Result("55301\nTRUE", Collections.emptyList()), result);
    }
}
