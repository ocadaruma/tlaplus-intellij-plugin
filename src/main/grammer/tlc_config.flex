package com.mayreh.intellij.plugin.tlc.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.openapi.util.text.LineColumn;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.mayreh.intellij.plugin.tlc.psi.TLCConfigElementTypes;

%%

%unicode
%public
%class _TLCConfigLexer
%implements FlexLexer
%function advance
%type IElementType

%state IN_BLOCK_COMMENT

WHITE_SPACE = " " | \t | \f | \R
IDENTIFIER = [0-9a-zA-Z_]* [a-zA-Z] [0-9a-zA-Z_]*

%%
<YYINITIAL> {
  "CONSTANT"           { return TLCConfigElementTypes.KEYWORD_CONSTANT; }
  "CONSTANTS"          { return TLCConfigElementTypes.KEYWORD_CONSTANTS; }
  "CONSTRAINT"         { return TLCConfigElementTypes.KEYWORD_CONSTRAINT; }
  "CONSTRAINTS"        { return TLCConfigElementTypes.KEYWORD_CONSTRAINTS; }
  "ACTION_CONSTRAINT"  { return TLCConfigElementTypes.KEYWORD_ACTION_CONSTRAINT; }
  "ACTION_CONSTRAINTS" { return TLCConfigElementTypes.KEYWORD_ACTION_CONSTRAINTS; }
  "INVARIANT"          { return TLCConfigElementTypes.KEYWORD_INVARIANT; }
  "INVARIANTS"         { return TLCConfigElementTypes.KEYWORD_INVARIANTS; }
  "INIT"               { return TLCConfigElementTypes.KEYWORD_INIT; }
  "NEXT"               { return TLCConfigElementTypes.KEYWORD_NEXT; }
  "VIEW"               { return TLCConfigElementTypes.KEYWORD_VIEW; }
  "SYMMETRY"           { return TLCConfigElementTypes.KEYWORD_SYMMETRY; }
  "SPECIFICATION"      { return TLCConfigElementTypes.KEYWORD_SPECIFICATION; }
  "PROPERTY"           { return TLCConfigElementTypes.KEYWORD_PROPERTY; }
  "PROPERTIES"         { return TLCConfigElementTypes.KEYWORD_PROPERTIES; }
  "ALIAS"              { return TLCConfigElementTypes.KEYWORD_ALIAS; }
  "POSTCONDITION"      { return TLCConfigElementTypes.KEYWORD_POSTCONDITION; }
  "CHECK_DEADLOCK"     { return TLCConfigElementTypes.KEYWORD_CHECK_DEADLOCK; }

  // literal
  "TRUE"  { return TLCConfigElementTypes.LITERAL_TRUE; }
  "FALSE" { return TLCConfigElementTypes.LITERAL_FALSE; }
  [0-9]+ | [0-9]+ "." [0-9]+ | \\[bB][01]+ | \\[oO][0-7]+ | \\[hH][0-9a-fA-F]+ {
    return TLCConfigElementTypes.LITERAL_NUMBER;
  }
  \" ([^\\\"\r\n\t] | \\[^\r\n\t])* \" { return TLCConfigElementTypes.LITERAL_STRING; }

  ","              { return TLCConfigElementTypes.COMMA; }
  "{"              { return TLCConfigElementTypes.LBRACE; }
  "}"              { return TLCConfigElementTypes.RBRACE; }
  "="              { return TLCConfigElementTypes.EQ; }
  "<-"             { return TLCConfigElementTypes.SUBST; }
  {IDENTIFIER}     { return TLCConfigElementTypes.IDENTIFIER; }

  // comments
  \\"*"[^\r\n]* { return TLCConfigElementTypes.COMMENT; }
  "(*"          { yybegin(IN_BLOCK_COMMENT); yypushback(2); }

  {WHITE_SPACE}+   { return TokenType.WHITE_SPACE; }
}

<IN_BLOCK_COMMENT> {
  "*)"    { yybegin(YYINITIAL); return TLCConfigElementTypes.COMMENT; }
  <<EOF>> { yybegin(YYINITIAL); return TLCConfigElementTypes.COMMENT; }
  [^]     {}
}

// catch all
[^] { return TokenType.BAD_CHARACTER; }
