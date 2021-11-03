package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Represents source location.
 *
 * The class doesn't correspond to actual psi yet, so you need to
 * locate the psi to use for navigation.
 */
@Value
@Accessors(fluent = true)
public class SourceLocation implements Comparable<SourceLocation> {
    private static final Comparator<SourceLocation> COMPARATOR =
            Comparator.comparing(SourceLocation::line)
                      .thenComparing(SourceLocation::col);

    // 0-origin
    int line;

    // 0-origin
    int col;

    @Override
    public int compareTo(@NotNull SourceLocation o) {
        return COMPARATOR.compare(this, o);
    }
}
