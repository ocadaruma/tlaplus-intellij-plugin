package com.mayreh.intellij.plugin.tlaplus.run.eval;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * The context that expression evaluation to be executed.
 * The module will be imported into dummy module for evaluation,
 * so it has to be valid.
 * Otherwise, the evaluation will fail.
 */
@Value
@Accessors(fluent = true)
public class Context {
    @NotNull String moduleName;
    @NotNull Path directory;
}
