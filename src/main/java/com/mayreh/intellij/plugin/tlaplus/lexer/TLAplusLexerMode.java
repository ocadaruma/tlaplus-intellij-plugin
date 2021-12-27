package com.mayreh.intellij.plugin.tlaplus.lexer;

import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_TLA_INITIAL;
import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_TLA_DEFAULT;
import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_PLUSCAL_DEFAULT;
import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_TLA_HANDLE_INDENT;
import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_PLUSCAL_HANDLE_INDENT;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TLAplusLexerMode {
    TLA(ZZ_TLA_INITIAL, ZZ_TLA_DEFAULT, ZZ_TLA_HANDLE_INDENT),
    TLA_FRAGMENT(ZZ_TLA_DEFAULT, ZZ_TLA_DEFAULT, ZZ_TLA_HANDLE_INDENT),
    PLUSCAL(ZZ_PLUSCAL_DEFAULT, ZZ_PLUSCAL_DEFAULT, ZZ_PLUSCAL_HANDLE_INDENT),
    ;

    public final int initialState;
    public final int defaultState;
    public final int handleIndentState;
}
