package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusLexer extends LayeredLexer {
    public TLAplusLexer(boolean forHighlighting) {
        super(new MergingLexerAdapter(
                new FlexAdapter(new _TLAplusLexer(forHighlighting, TLAplusLexerMode.TLA)),
                TokenSet.create(TLAplusElementTypes.COMMENT)));

        registerLayer(
                new MergingLexerAdapter(
                        new FlexAdapter(
                                new _TLAplusPlusCalCommentLexer(null)),
                        TokenSet.create(TLAplusElementTypes.COMMENT)),
                TLAplusElementTypes.COMMENT);
    }
}
