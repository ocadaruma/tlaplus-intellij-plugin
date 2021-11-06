package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.openapi.util.text.LineColumn;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.intellij.util.containers.Stack;
import com.mayreh.intellij.plugin.tlaplus.lexer.JunctionIndentation.Type;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

%%

%unicode
%public
%class _TLCErrorTraceLexer
%implements FlexLexer
%function advance
%type IElementType

WHITE_SPACE = " " | \t | \f | \R
IDENTIFIER = [0-9a-zA-Z_]* [a-zA-Z] [0-9a-zA-Z_]*

%%
<YYINITIAL> {
  "/\\"            { return TLAplusElementTypes.OP_LAND2; }
  "<<"             { return TLAplusElementTypes.LTUPLE; }
  ">>"             { return TLAplusElementTypes.RTUPLE; }
  ","              { return TLAplusElementTypes.COMMA; }
  "("              { return TLAplusElementTypes.LPAREN; }
  ")"              { return TLAplusElementTypes.RPAREN; }
  "{"              { return TLAplusElementTypes.LBRACKET; }
  "}"              { return TLAplusElementTypes.RBRACKET; }
  "["              { return TLAplusElementTypes.LSQBRACKET; }
  "]"              { return TLAplusElementTypes.RSQBRACKET; }
  "@@"             { return TLAplusElementTypes.OP_ATAT; }
  "|->"            { return TLAplusElementTypes.OP_VBARDASHGT; }
  ":>"             { return TLAplusElementTypes.OP_COLONGT; }
  "="              { return TLAplusElementTypes.OP_EQ; }
  {IDENTIFIER}     { return TLAplusElementTypes.IDENTIFIER; }
  [-.0-9a-zA-Z_]+  { return TLAplusElementTypes.TLC_ERROR_TRACE_PRIMITIVE; }
  \" ([^\\\"\r\n\t] | \\[^\r\n\t])* \" { return TLAplusElementTypes.LITERAL_STRING; }
  {WHITE_SPACE}+   { return TokenType.WHITE_SPACE; }
}

// catch all
[^] { return TokenType.BAD_CHARACTER; }
