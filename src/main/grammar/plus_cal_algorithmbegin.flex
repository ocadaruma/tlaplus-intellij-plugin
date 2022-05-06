package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

%%

%unicode
%public
%class _PlusCalAlgorithmBeginLexer
%implements FlexLexer
%function advance
%type IElementType

WHITE_SPACE = " " | \t | \f | \R

%%
<YYINITIAL> {
  --             { return TLAplusElementTypes.SEPARATOR; }
  algorithm      { return TLAplusElementTypes.PLUS_CAL_KW_ALGORITHM; }
  fair           { return TLAplusElementTypes.PLUS_CAL_KW_FAIR; }
  {WHITE_SPACE}+ { return TokenType.WHITE_SPACE; }
}

// catch all
[^] { return TokenType.BAD_CHARACTER; }
