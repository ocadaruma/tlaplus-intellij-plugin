package com.mayreh.intellij.plugin.tlc.lexer;

import com.intellij.lexer.FlexAdapter;

public class TLCConfigLexer extends FlexAdapter {
    public TLCConfigLexer() {
        super(new _TLCConfigLexer(null));
    }
}
