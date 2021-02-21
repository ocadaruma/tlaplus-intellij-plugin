package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

%%

%unicode
%public
%class _TLAplusModuleBeginLexer
%implements FlexLexer
%function advance
%type IElementType

WHITE_SPACE = " " | \t | \f | \R

%%
<YYINITIAL> {
  ---- -*        { return TLAplusElementTypes.SEPARATOR; }
  {WHITE_SPACE}+ { return TokenType.WHITE_SPACE; }
  "MODULE"       { return TLAplusElementTypes.KEYWORD_MODULE; }
}

// catch all
[^] { return TokenType.BAD_CHARACTER; }
