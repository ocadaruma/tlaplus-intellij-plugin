package com.mayreh.intellij.plugin.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilTest {
    @Test
    public void testOffsetToColumn() {
        assertEquals(0, StringUtil.offsetToColumn("abcdef\nghijkl", 7));
        assertEquals(1, StringUtil.offsetToColumn("abcdef\nghijkl", 8));
        assertEquals(2, StringUtil.offsetToColumn("abcdef\nghijkl", 2));
        assertEquals(-1, StringUtil.offsetToColumn("abcdef\nghijkl", 999));

        assertEquals(0, StringUtil.offsetToColumn("abcdef\r\nghijkl", 8));
    }
}
