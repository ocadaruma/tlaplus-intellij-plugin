package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

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

    public static JunctionIndentation from(IElementType elementType, int column) {
        if (elementType == TLAplusElementTypes.OP_LOR2) {
            return new JunctionIndentation(Type.Or, column);
        } else if (elementType == TLAplusElementTypes.OP_LAND2) {
            return new JunctionIndentation(Type.And, column);
        } else {
            throw new IllegalArgumentException("Unknown junction type: " + elementType);
        }
    }
}
