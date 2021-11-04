package com.mayreh.intellij.plugin.tlaplus;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class TestUtils {

    public static String resourceToString(String name) {
        try (InputStream stream = TestUtils.class.getClassLoader().getResourceAsStream(name)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
