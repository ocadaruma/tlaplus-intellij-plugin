package com.mayreh.intellij.plugin.tlaplus.run.ui;

import com.mayreh.intellij.plugin.tlaplus.run.parsing.SourceLocation;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class ActionFormula {
    String module;
    String action;
    SourceLocation location;
}
