package com.mayreh.intellij.plugin.tlc.psi;

import com.intellij.psi.tree.IElementType;
import com.mayreh.intellij.plugin.tlc.TLCConfigLanguage;

public class TLCConfigTokenType extends IElementType {
    public TLCConfigTokenType(String debugName) {
        super(debugName, TLCConfigLanguage.INSTANCE);
    }
}
