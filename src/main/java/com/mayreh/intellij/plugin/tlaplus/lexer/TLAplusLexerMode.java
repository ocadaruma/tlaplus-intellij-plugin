package com.mayreh.intellij.plugin.tlaplus.lexer;

import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_TLA_INITIAL;
import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_TLA_DEFAULT;
import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_PLUSCAL_DEFAULT;
import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_TLA_HANDLE_INDENT;
import static com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusLexer.ZZ_PLUSCAL_HANDLE_INDENT;

import lombok.RequiredArgsConstructor;

/**
 * {@link _TLAplusLexer} is expected to be used to tokenize
 * ordinary TLA+ spec, TLA+ code fragment, and PlusCal algorithm because they almost share their tokens.
 *
 * However, tokenize logic as well as and tokens are slightly different, so we specify lexer mode
 * as the constructor parameter.
 */
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
