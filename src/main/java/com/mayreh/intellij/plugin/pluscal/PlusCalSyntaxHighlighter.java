package com.mayreh.intellij.plugin.pluscal;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.Lexer;
import com.mayreh.intellij.plugin.tlaplus.TLAplusSyntaxHighlighter;

public class PlusCalSyntaxHighlighter extends TLAplusSyntaxHighlighter {
    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new PlusCalLexer(true);
    }
}
