package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusTokenType;

%%

%unicode
%public
%class TLAplusLexer
%implements FlexLexer
%function advance
%type IElementType

%state IN_MODULE
%state TERMINATED

WHITE_SPACE = " " | \t | \f
ONE_NL = \R
NLS = {ONE_NL}({ONE_NL}|{WHITE_SPACE})*
LINE_COMMENT = "/""/"[^\r\n]*
BLOCK_COMMENT = "(*" ("*"? [^\)])* "*)"
SEPARATOR = ---- -*

%%
<YYINITIAL> {
  {SEPARATOR} ({WHITE_SPACE} | {NLS})* "MODULE" { yypushback(yylength()); yybegin(IN_MODULE); }
  [^]* { return TLAplusTokenType.IGNORED; }
}

<IN_MODULE> {
  // keywords
  "ASSUME"     { return TLAplusTokenType.KEYWORD_ASSUME; }
  "ELSE"       { return TLAplusTokenType.KEYWORD_ELSE; }
  "LOCAL"      { return TLAplusTokenType.KEYWORD_LOCAL; }
  "ASSUMPTION" { return TLAplusTokenType.KEYWORD_ASSUMPTION; }
  "MODULE"     { return TLAplusTokenType.KEYWORD_MODULE; }
  "VARIABLE"   { return TLAplusTokenType.KEYWORD_VARIABLE; }
  "VARIABLES"  { return TLAplusTokenType.KEYWORD_VARIABLES; }
  "AXIOM"      { return TLAplusTokenType.KEYWORD_AXIOM; }
  "EXCEPT"     { return TLAplusTokenType.KEYWORD_EXCEPT; }
  "OTHER"      { return TLAplusTokenType.KEYWORD_OTHER; }
  "CASE"       { return TLAplusTokenType.KEYWORD_CASE; }
  "EXTENDS"    { return TLAplusTokenType.KEYWORD_EXTENDS; }
  "SF_"        { return TLAplusTokenType.KEYWORD_SF_; }
  "WF_"        { return TLAplusTokenType.KEYWORD_WF_; }
  "CHOOSE"     { return TLAplusTokenType.KEYWORD_CHOOSE; }
  "IF"         { return TLAplusTokenType.KEYWORD_IF; }
  "WITH"       { return TLAplusTokenType.KEYWORD_WITH; }
  "CONSTANTS"  { return TLAplusTokenType.KEYWORD_CONSTANTS; }
  "CONSTANT"   { return TLAplusTokenType.KEYWORD_CONSTANT; }
  "IN"         { return TLAplusTokenType.KEYWORD_IN; }
  "THEN"       { return TLAplusTokenType.KEYWORD_THEN; }
  "INSTANCE"   { return TLAplusTokenType.KEYWORD_INSTANCE; }
  "THEOREM"    { return TLAplusTokenType.KEYWORD_THEOREM; }
  "LET"        { return TLAplusTokenType.KEYWORD_LET; }

  // quantifier
  \\EE         { return TLAplusTokenType.QUANTIFIER_EE; }
  \\E          { return TLAplusTokenType.QUANTIFIER_E; }
  \\AA         { return TLAplusTokenType.QUANTIFIER_AA; }
  \\A          { return TLAplusTokenType.QUANTIFIER_A; }

  // comments
  {LINE_COMMENT} { return TLAplusTokenType.COMMENT_LINE; }

  {SEPARATOR} { return TLAplusTokenType.SEPARATOR; }
  ==== =*     { yybegin(TERMINATED); return TLAplusTokenType.MODULE_END; }
  [^]         { return TokenType.BAD_CHARACTER; }
}

<TERMINATED> {
  [^]* { return TLAplusTokenType.IGNORED; }
}
