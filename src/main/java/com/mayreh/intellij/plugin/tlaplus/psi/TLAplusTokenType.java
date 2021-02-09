package com.mayreh.intellij.plugin.tlaplus.psi;

import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;

public class TLAplusTokenType extends IElementType {
    public TLAplusTokenType(String debugName) {
        super(debugName, TLAplusLanguage.INSTANCE);
    }
}
