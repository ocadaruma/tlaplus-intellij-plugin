package com.mayreh.intellij.plugin.tlaplus;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusQuoteHandler extends SimpleTokenSetQuoteHandler {
    public TLAplusQuoteHandler() {
        super(TLAplusElementTypes.LITERAL_STRING);
    }
}
