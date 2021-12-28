package com.mayreh.intellij.plugin.tlaplus.fragment;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.tlaplus.TLAplusSyntaxHighlighter;

public class TLAplusFragmentSyntaxHighlighter extends TLAplusSyntaxHighlighter {
    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new TLAplusFragmentLexer(true);
    }
}
