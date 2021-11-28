package com.mayreh.intellij.plugin.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class StringUtil {
    public static @NotNull String joinLines(@NotNull List<String> lines) {
        StringWriter sw = new StringWriter();
        try (PrintWriter writer = new PrintWriter(sw, true)) {
            lines.forEach(writer::println);
        }
        return sw.toString();
    }
}
