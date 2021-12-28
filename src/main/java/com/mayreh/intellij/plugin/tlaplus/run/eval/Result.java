package com.mayreh.intellij.plugin.tlaplus.run.eval;

import java.util.List;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Represents the result of evaluation.
 */
@Value
@Accessors(fluent = true)
public class Result {
    String output;
    List<String> errors;
}
