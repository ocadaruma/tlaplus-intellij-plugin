package com.mayreh.intellij.plugin.tlaplus.run.eval;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Dummy module for evaluating the expression.
 */
@Value
@Accessors(fluent = true)
public class DummyModule {
    private static final String MODULE_NAME = "__dummy_module__";
    private static final String INIT_OP_NAME = "replinit";
    private static final String NEXT_OP_NAME = "replnext";
    private static final String VALUE_OP_NAME = "replvalue";

    Path moduleFile;

    public static String moduleName() {
        return MODULE_NAME;
    }

    public static String valueOperatorName() {
        return VALUE_OP_NAME;
    }

    public static String configFileContent() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("INIT ").append(INIT_OP_NAME).append('\n')
              .append("NEXT ").append(NEXT_OP_NAME);
        return buffer.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String> extendList = new ArrayList<>();
        private boolean doEvaluation = true;
        private String expression = "";

        public Builder extend(String moduleName) {
            extendList.add(moduleName);
            return this;
        }

        /**
         * Configure this module for completion rather than actual expression evaluation.
         * If called, resulting module's body will be left empty so that pointless variables or operators
         * (e.g. replvalue) do not appear in completion candidate.
         */
        public Builder forCompletion() {
            doEvaluation = false;
            return this;
        }

        public Builder setExpression(String expression) {
            this.expression = expression;
            return this;
        }

        public String buildAsString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("---- MODULE ").append(MODULE_NAME).append(" ----").append('\n')
                  .append("EXTENDS Reals,Sequences,Bags,FiniteSets,TLC,Randomization");
            if (!extendList.isEmpty()) {
                buffer.append(',');
                buffer.append(String.join(",", extendList));
            }
            buffer.append('\n');

            if (doEvaluation) {
                buffer.append("VARIABLE replvar").append('\n');
                buffer.append(INIT_OP_NAME).append(" == replvar = 0").append('\n');
                buffer.append(NEXT_OP_NAME).append(" == replvar' = 0").append('\n');
                buffer.append(VALUE_OP_NAME).append(" == ").append(expression).append('\n');
            }

            buffer.append('\n');
            buffer.append("====");
            return buffer.toString();
        }
    }
}
