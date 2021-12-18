package com.mayreh.intellij.plugin.tlaplus.run.eval;

import java.nio.file.Path;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Dummy module for evaluating the expression.
 */
@Value
@Accessors(fluent = true)
public class DummyModule {
    Path moduleFile;

    public static String moduleName() {
        return "__dummy_module__";
    }
}
