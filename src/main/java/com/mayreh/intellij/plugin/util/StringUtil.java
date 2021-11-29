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

    /**
     * Get column from the offset inside the text.
     *
     * This should be faster than {@link com.intellij.openapi.util.text.StringUtil#offsetToLineColumn} which
     * traverses text from the beginning.
     */
    public static int offsetToColumn(@NotNull CharSequence text, int offset) {
        if (offset >= text.length()) {
            return -1;
        }

        // this should work even for \r\n
        int newlineOffset = com.intellij.openapi.util.text.StringUtil.lastIndexOf(text, '\n', 0, offset);
        if (newlineOffset < 0) {
            return offset;
        }
        return offset - (newlineOffset + 1);
    }
}
