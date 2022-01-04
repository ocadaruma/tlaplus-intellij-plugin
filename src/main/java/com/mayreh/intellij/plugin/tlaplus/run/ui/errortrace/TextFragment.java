package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import org.jetbrains.annotations.Nullable;

import com.mayreh.intellij.plugin.tlaplus.run.ui.ActionFormula;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
class TextFragment {
    String text;
    @Nullable ActionFormula actionFormula;
}
