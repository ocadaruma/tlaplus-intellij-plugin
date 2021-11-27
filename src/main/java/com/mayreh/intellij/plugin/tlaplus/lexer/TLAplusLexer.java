package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusLexer extends LayeredLexer {
    public TLAplusLexer(boolean forHighlighting) {
        super(new MergingLexerAdapter(
                new FlexAdapter(new _TLAplusLexer(forHighlighting)),
                TokenSet.create(TLAplusElementTypes.IGNORED)));

        registerLayer(
                new MergingLexerAdapter(
                        new FlexAdapter(
                                new _TLAplusPlusCalCommentLexer(null)),
                        TokenSet.create(TLAplusElementTypes.IGNORED)),
                TLAplusElementTypes.IGNORED,
                TLAplusElementTypes.COMMENT_LINE,
                TLAplusElementTypes.COMMENT_BLOCK);
    }
}
