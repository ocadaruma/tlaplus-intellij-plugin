package com.mayreh.intellij.plugin.tlaplus.run.eval;

import java.nio.file.Path;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * The context that expression evaluation to be executed.
 * This module will be imported into dummy module for evaluation,
 * so it has to be valid.
 * Otherwise, the evaluation will fail.
 */
@Value
@Accessors(fluent = true)
public class Context {
    String moduleName;
    Path directory;
}
