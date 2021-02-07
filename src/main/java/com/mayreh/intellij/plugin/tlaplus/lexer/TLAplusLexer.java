package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexAdapter;

public class TLAplusLexer extends FlexAdapter {
    public TLAplusLexer() {
        super(new _TLAplusLexer(null));
    }
}
