package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public interface TokenSets {
    TokenSet KEYWORDS = TokenSet.create(
            TLAplusElementTypes.KEYWORD_ASSUME,
            TLAplusElementTypes.KEYWORD_ASSUMPTION,
            TLAplusElementTypes.KEYWORD_AXIOM,
            TLAplusElementTypes.KEYWORD_CASE,
            TLAplusElementTypes.KEYWORD_CHOOSE,
            TLAplusElementTypes.KEYWORD_CONSTANT,
            TLAplusElementTypes.KEYWORD_CONSTANTS,
            TLAplusElementTypes.KEYWORD_ELSE,
            TLAplusElementTypes.KEYWORD_EXCEPT,
            TLAplusElementTypes.KEYWORD_EXTENDS,
            TLAplusElementTypes.KEYWORD_IF,
            TLAplusElementTypes.KEYWORD_IN,
            TLAplusElementTypes.KEYWORD_INSTANCE,
            TLAplusElementTypes.KEYWORD_LET,
            TLAplusElementTypes.KEYWORD_LOCAL,
            TLAplusElementTypes.KEYWORD_MODULE,
            TLAplusElementTypes.KEYWORD_OTHER,
            TLAplusElementTypes.KEYWORD_SF_,
            TLAplusElementTypes.KEYWORD_THEN,
            TLAplusElementTypes.KEYWORD_THEOREM,
            TLAplusElementTypes.KEYWORD_VARIABLE,
            TLAplusElementTypes.KEYWORD_VARIABLES,
            TLAplusElementTypes.KEYWORD_WF_,
            TLAplusElementTypes.KEYWORD_WITH
    );
}
