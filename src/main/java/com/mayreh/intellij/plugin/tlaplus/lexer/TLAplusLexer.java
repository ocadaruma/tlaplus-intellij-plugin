package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusLexer extends MergingLexerAdapter {
    public TLAplusLexer(boolean forHighlighting) {
        super(new FlexAdapter(new _TLAplusLexer(forHighlighting)),
              TokenSet.create(TLAplusElementTypes.IGNORED));
    }
}
