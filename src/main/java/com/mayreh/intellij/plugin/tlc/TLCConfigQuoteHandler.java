package com.mayreh.intellij.plugin.tlc;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.mayreh.intellij.plugin.tlc.psi.TLCConfigElementTypes;

public class TLCConfigQuoteHandler extends SimpleTokenSetQuoteHandler {
    public TLCConfigQuoteHandler() {
        super(TLCConfigElementTypes.LITERAL_STRING);
    }
}
