package com.mayreh.intellij.plugin.tlc.psi;

import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlc.TLCConfigLanguage;

public class TLCConfigElementType extends IElementType {
    public TLCConfigElementType(String debugName) {
        super(debugName, TLCConfigLanguage.INSTANCE);
    }
}
