package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusTokenType;

%%

%unicode
%public
%class _TLAplusLexer
%extends TLAplusLexerBase
%function advance
%type IElementType

%state IN_MODULE
%state IN_BLOCK_COMMENT
%state TERMINATED

WHITE_SPACE = " " | \t | \f
ONE_NL = \R
NLS = {ONE_NL} ({ONE_NL} | {WHITE_SPACE})*
SEPARATOR = ---- -*
MODULE_BEGIN = {SEPARATOR} ({WHITE_SPACE} | {NLS})* "MODULE"

%%
<YYINITIAL> {
  {MODULE_BEGIN} { yypushback(yylength()); yybegin(IN_MODULE); }
  [^]+ {MODULE_BEGIN} {
    pushbackModuleBegin();
    yybegin(IN_MODULE);
    return TLAplusTokenType.IGNORED;
  }
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

  // literal
  [0-9]+ | [0-9]+ "." [0-9]+ | \\[bB][01]+ | \\[oO][0-7]+ | \\[hH][0-9a-fA-F]+ {
    return TLAplusTokenType.LITERAL_NUMBER;
  }
  \" ([^\\\"\r\n] | \\[^\r\n])* \" { return TLAplusTokenType.LITERAL_STRING; }

  // identifier
  [0-9a-zA-Z_]* [a-zA-Z] [0-9a-zA-Z_]* { return TLAplusTokenType.IDENTIFIER; }

  // symbols
  "<<"  { return TLAplusTokenType.LTUPLE; }
  ">>"  { return TLAplusTokenType.RTUPLE; }
  ">>_" { return TLAplusTokenType.RTUPLEUNDER; }
  ","   { return TLAplusTokenType.COMMA; }
  "("   { return TLAplusTokenType.LPARENTHESIS; }
  ")"   { return TLAplusTokenType.RPARENTHESIS; }
  "{"   { return TLAplusTokenType.LBRACKET; }
  "}"   { return TLAplusTokenType.RBRACKET; }
  "["   { return TLAplusTokenType.LSQBRACKET; }
  "]"   { return TLAplusTokenType.RSQBRACKET; }
  "]_"  { return TLAplusTokenType.RSQBRACKETUNDER; }
  "."   { return TLAplusTokenType.DOT; }
  ":"   { return TLAplusTokenType.COLON; }

  // define
  "==" { return TLAplusTokenType.DEFINE; }

  // operators
  "ENABLED" ({NLS} | {WHITE_SPACE})   { return TLAplusTokenType.OP_ENABLED; }
  "UNCHANGED" ({NLS} | {WHITE_SPACE}) { return TLAplusTokenType.OP_UNCHANGED; }
  "SUBSET" ({NLS} | {WHITE_SPACE})    { return TLAplusTokenType.OP_SUBSET; }
  "DOMAIN" ({NLS} | {WHITE_SPACE})    { return TLAplusTokenType.OP_ENABLED; }
  "~" | \\neg | \\lnot { return TLAplusTokenType.OP_NEG; }
  "[]"                 { return TLAplusTokenType.OP_SQUARE; }
  "<>"                 { return TLAplusTokenType.OP_DIAMOND; }
  -                    { return TLAplusTokenType.OP_DASH; }
  "/\\" | \\land       { return TLAplusTokenType.OP_LAND; }
  \\in                 { return TLAplusTokenType.OP_IN; }
  "<"                  { return TLAplusTokenType.OP_LT; }
  "=<" | "<=" | \\leq  { return TLAplusTokenType.OP_LTEQ; }
  \\ll                 { return TLAplusTokenType.OP_LTLT; }
  \\prec               { return TLAplusTokenType.OP_PREC; }
  \\preceq             { return TLAplusTokenType.OP_PRECEQ; }
  \\subseteq           { return TLAplusTokenType.OP_SUBSETEQ; }
  \\subset             { return TLAplusTokenType.OP_SUBSET; }
  \\sqsubset           { return TLAplusTokenType.OP_SQSUBSET; }
  \\sqsubseteq         { return TLAplusTokenType.OP_SQSUBSETEQ; }
  "|-"                 { return TLAplusTokenType.OP_VBARDASH; }
  "|="                 { return TLAplusTokenType.OP_VBAREQ; }
  "->"                 { return TLAplusTokenType.OP_DASHGT; }
  \\cap | \\intersect  { return TLAplusTokenType.OP_CAP; }
  \\sqcap              { return TLAplusTokenType.OP_SQCAP; }
  "(+)" | \\oplus      { return TLAplusTokenType.OP_OPLUS; }
  "(-)" | \\ominus     { return TLAplusTokenType.OP_OMINUS; }
  "(.)" | \\odot       { return TLAplusTokenType.OP_ODOT; }
  "(\\X)" | \\otimes   { return TLAplusTokenType.OP_OTIMES; }
  "(/)" | \\oslash     { return TLAplusTokenType.OP_OSLASH; }
  "\\/" | \\lor        { return TLAplusTokenType.OP_LOR; }
  "<=>" | \\equiv      { return TLAplusTokenType.OP_EQUIV; }
  \\notin              { return TLAplusTokenType.OP_NOTIN; }
  ">"                  { return TLAplusTokenType.OP_GT; }
  ">=" | \\geq         { return TLAplusTokenType.OP_GTEQ; }
  \\gg                 { return TLAplusTokenType.OP_GTGT; }
  \\succ               { return TLAplusTokenType.OP_SUCC; }
  \\succeq             { return TLAplusTokenType.OP_SUCCEQ; }
  \\supseteq           { return TLAplusTokenType.OP_SUPSETEQ; }
  \\supset             { return TLAplusTokenType.OP_SUPSET; }
  \\sqsupset           { return TLAplusTokenType.OP_SQSUPSET; }
  \\sqsupseteq         { return TLAplusTokenType.OP_SQSUPSETEQ; }
  "-|"                 { return TLAplusTokenType.OP_DASHVBAR; }
  "=|"                 { return TLAplusTokenType.OP_EQVBAR; }
  "<-"                 { return TLAplusTokenType.OP_LTDASH; }
  \\cup | \\union      { return TLAplusTokenType.OP_CUP; }
  \\sqcup              { return TLAplusTokenType.OP_SQCUP; }
  \\uplus              { return TLAplusTokenType.OP_UPLUS; }
  \\X | \\times        { return TLAplusTokenType.OP_X; }
  \\wr                 { return TLAplusTokenType.OP_WR; }
  \\propto             { return TLAplusTokenType.OP_PROPTO; }
  "=>"                 { return TLAplusTokenType.OP_EQGT; }
  "="                  { return TLAplusTokenType.OP_EQ; }
  "/=" | #             { return TLAplusTokenType.OP_NOTEQ; }
  "->"                 { return TLAplusTokenType.OP_DASHGT; }
  "-+->"               { return TLAplusTokenType.OP_DASHPLUSDASHGT; }
  "|->"                { return TLAplusTokenType.OP_VBARDASHGT; }
  \\div                { return TLAplusTokenType.OP_DIV; }
  \\cdot               { return TLAplusTokenType.OP_CDOT; }
  \\circ | \\o         { return TLAplusTokenType.OP_CIRC; }
  \\bullet             { return TLAplusTokenType.OP_BULLET; }
  \\star               { return TLAplusTokenType.OP_STAR; }
  \\bigcirc            { return TLAplusTokenType.OP_BIGCIRC; }
  \\sim                { return TLAplusTokenType.OP_SIM; }
  \\simeq              { return TLAplusTokenType.OP_SIMEQ; }
  \\asymp              { return TLAplusTokenType.OP_ASYMP; }
  \\approx             { return TLAplusTokenType.OP_APPROX; }
  \\cong               { return TLAplusTokenType.OP_CONG; }
  \\doteq              { return TLAplusTokenType.OP_DOTEQ; }
  "!!"                 { return TLAplusTokenType.OP_BANGBANG; }
  ##                   { return TLAplusTokenType.OP_SHARPSHARP; }
  "$"                  { return TLAplusTokenType.OP_DOLLAR; }
  "$$"                 { return TLAplusTokenType.OP_DOLLARDOLLAR; }
  %                    { return TLAplusTokenType.OP_PERCENT; }
  %%                   { return TLAplusTokenType.OP_PERCENTPERCENT; }
  &                    { return TLAplusTokenType.OP_AMP; }
  &&                   { return TLAplusTokenType.OP_AMPAMP; }
  "*"                  { return TLAplusTokenType.OP_ASTER; }
  "**"                 { return TLAplusTokenType.OP_ASTERASTER; }
  "+"                  { return TLAplusTokenType.OP_PLUS; }
  "++"                 { return TLAplusTokenType.OP_PLUSPLUS; }
  --                   { return TLAplusTokenType.OP_DASHDASH; }
  ".."                 { return TLAplusTokenType.OP_DOTDOT; }
  "..."                { return TLAplusTokenType.OP_DOTDOTDOT; }
  "/"                  { return TLAplusTokenType.OP_SLASH; }
  "//"                 { return TLAplusTokenType.OP_SLASHSLASH; }
  ::=                  { return TLAplusTokenType.OP_COLONCOLONEQ; }
  :=                   { return TLAplusTokenType.OP_COLONEQ; }
  :>                   { return TLAplusTokenType.OP_COLONGT; }
  "<:"                 { return TLAplusTokenType.OP_LTCOLON; }
  "?"                  { return TLAplusTokenType.OP_QUERY; }
  "??"                 { return TLAplusTokenType.OP_QUERYQUERY; }
  \\                   { return TLAplusTokenType.OP_SUBTRACT; }
  "^"                  { return TLAplusTokenType.OP_CARET; }
  "^^"                 { return TLAplusTokenType.OP_CARETCARET; }
  "|"                  { return TLAplusTokenType.OP_VBAR; }
  "||"                 { return TLAplusTokenType.OP_VBARVBAR; }
  \'                   { return TLAplusTokenType.OP_PRIME; }
  "^+"                 { return TLAplusTokenType.OP_CARETPLUS; }
  "^*"                 { return TLAplusTokenType.OP_CARETASTER; }
  "^#"                 { return TLAplusTokenType.OP_CARETSHARP; }

  // comments
  "/""/"[^\r\n]* { return TLAplusTokenType.COMMENT_LINE; }
  "(*"           { yybegin(IN_BLOCK_COMMENT); yypushback(2); }

  {NLS}         { return TLAplusTokenType.NL; }
  {WHITE_SPACE} { return TokenType.WHITE_SPACE; }
  {SEPARATOR}   { return TLAplusTokenType.SEPARATOR; }
  ==== =*       { yybegin(TERMINATED); return TLAplusTokenType.MODULE_END; }
  [^]           { return TokenType.BAD_CHARACTER; }
}

<IN_BLOCK_COMMENT> {
  "*)" { yybegin(IN_MODULE); return TLAplusTokenType.COMMENT_BLOCK; }
  [^]  {}
}

<TERMINATED> {
  [^]* { return TLAplusTokenType.IGNORED; }
}
