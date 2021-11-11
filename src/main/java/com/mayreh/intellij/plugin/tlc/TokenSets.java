package com.mayreh.intellij.plugin.tlc;

import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlc.psi.TLCConfigElementTypes;

public interface TokenSets {
    TokenSet KEYWORDS = TokenSet.create(
            TLCConfigElementTypes.KEYWORD_CONSTANT,
            TLCConfigElementTypes.KEYWORD_CONSTANTS,
            TLCConfigElementTypes.KEYWORD_CONSTRAINT,
            TLCConfigElementTypes.KEYWORD_CONSTRAINTS,
            TLCConfigElementTypes.KEYWORD_ACTION_CONSTRAINT,
            TLCConfigElementTypes.KEYWORD_ACTION_CONSTRAINTS,
            TLCConfigElementTypes.KEYWORD_INVARIANT,
            TLCConfigElementTypes.KEYWORD_INVARIANTS,
            TLCConfigElementTypes.KEYWORD_INIT,
            TLCConfigElementTypes.KEYWORD_NEXT,
            TLCConfigElementTypes.KEYWORD_VIEW,
            TLCConfigElementTypes.KEYWORD_SYMMETRY,
            TLCConfigElementTypes.KEYWORD_SPECIFICATION,
            TLCConfigElementTypes.KEYWORD_PROPERTY,
            TLCConfigElementTypes.KEYWORD_PROPERTIES,
            TLCConfigElementTypes.KEYWORD_ALIAS,
            TLCConfigElementTypes.KEYWORD_POSTCONDITION,
            TLCConfigElementTypes.KEYWORD_CHECK_DEADLOCK);

    TokenSet OPERATORS = TokenSet.create(
            TLCConfigElementTypes.EQ,
            TLCConfigElementTypes.SUBST);
}
