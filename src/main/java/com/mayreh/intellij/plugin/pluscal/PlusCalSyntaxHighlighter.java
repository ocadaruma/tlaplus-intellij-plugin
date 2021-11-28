package com.mayreh.intellij.plugin.pluscal;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.tlaplus.TLAplusSyntaxHighlighter;
import com.mayreh.intellij.plugin.tlaplus.lexer._PlusCalAlgorithmBeginLexer;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class PlusCalSyntaxHighlighter extends TLAplusSyntaxHighlighter {
    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new PlusCalHighlightingLexer();
    }

    public static class PlusCalHighlightingLexer extends LayeredLexer {
        public PlusCalHighlightingLexer() {
            super(new PlusCalLexer(true));

            registerLayer(new FlexAdapter(new _PlusCalAlgorithmBeginLexer(null)),
                          TLAplusElementTypes.PLUS_CAL_ALGORITHM_BEGIN);
        }
    }
}
