package com.mayreh.intellij.plugin.util;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

public class Optionalx {
    /**
     * If the object of type {@link T} is instance of {@link U}, return casted object.
     * Otherwise, return false.
     */
    public static <T, U> Optional<U> asInstanceOf(@Nullable T obj, Class<U> clazz) {
        if (clazz.isInstance(obj)) {
            return Optional.of(clazz.cast(obj));
        }
        return Optional.empty();
    }
}
