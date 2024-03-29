package com.mayreh.intellij.plugin.pluscal;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusLexerMode;
import com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class PlusCalLexer extends MergingLexerAdapter {
    public PlusCalLexer(boolean forHighlighting) {
        super(new FlexAdapter(new _TLAplusLexer(forHighlighting, TLAplusLexerMode.PLUSCAL)),
              TokenSet.create(TLAplusElementTypes.COMMENT));
    }
}
