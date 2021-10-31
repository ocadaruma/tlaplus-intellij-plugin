package com.mayreh.intellij.plugin.tlaplus.lexer;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class JunctionIndentation {
    public enum Type {
        And,
        Or,
    }

    Type type;
    int column;

    public static JunctionIndentation and(int column) {
        return new JunctionIndentation(Type.And, column);
    }

    public static JunctionIndentation or(int column) {
        return new JunctionIndentation(Type.Or, column);
    }
}
