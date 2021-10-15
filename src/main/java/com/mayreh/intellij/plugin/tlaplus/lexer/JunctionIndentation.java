package com.mayreh.intellij.plugin.tlaplus.lexer;

import java.util.Objects;

public class JunctionIndentation {
    public enum Type {
        And,
        Or,
    }

    public final Type type;
    public final int column;

    public JunctionIndentation(Type type, int column) {
        this.type = type;
        this.column = column;
    }

    public static JunctionIndentation and(int column) {
        return new JunctionIndentation(Type.And, column);
    }

    public static JunctionIndentation or(int column) {
        return new JunctionIndentation(Type.Or, column);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final JunctionIndentation that = (JunctionIndentation) o;
        return column == that.column && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, column);
    }
}
