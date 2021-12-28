package com.mayreh.intellij.plugin.tlaplus.fragment;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusLexerMode;
import com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusFragmentLexer extends MergingLexerAdapter {
    public TLAplusFragmentLexer(boolean forHighlighting) {
        super(new FlexAdapter(new _TLAplusLexer(forHighlighting, TLAplusLexerMode.TLA_FRAGMENT)),
              TokenSet.create(TLAplusElementTypes.COMMENT));
    }
}
