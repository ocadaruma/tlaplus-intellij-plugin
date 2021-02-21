package com.mayreh.intellij.plugin.tlaplus.psi;

import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;
import com.mayreh.intellij.plugin.tlaplus.parser.TLAplusParser;

/**
 * Represents an AST node of TLA+, parsed by {@link TLAplusParser}.
 */
public class TLAplusElementType extends IElementType {
    public TLAplusElementType(String debugName) {
        super(debugName, TLAplusLanguage.INSTANCE);
    }
}
