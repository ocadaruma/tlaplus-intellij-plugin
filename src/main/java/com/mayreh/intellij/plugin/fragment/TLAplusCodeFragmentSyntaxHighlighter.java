package com.mayreh.intellij.plugin.fragment;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.tlaplus.TLAplusSyntaxHighlighter;
import com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusCodeFragmentLexer;

public class TLAplusCodeFragmentSyntaxHighlighter extends TLAplusSyntaxHighlighter {
    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new FlexAdapter(new _TLAplusCodeFragmentLexer(true));
    }
}
