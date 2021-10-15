package com.mayreh.intellij.plugin.tlaplus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class TestUtils {

    public static String resourceToString(String name) {
        try (InputStream stream = TestUtils.class.getClassLoader().getResourceAsStream(name);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];

            int read;
            while ((read = stream.read(buffer)) >= 0) {
                baos.write(buffer, 0, read);
            }

            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
