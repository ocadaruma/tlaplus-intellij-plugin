package com.mayreh.intellij.plugin.tlaplus;

import com.intellij.psi.tree.IElementType;

public class TLAplusElementType extends IElementType {
    public TLAplusElementType(String debugName) {
        super(debugName, TLAplusLanguage.INSTANCE);
    }
}
