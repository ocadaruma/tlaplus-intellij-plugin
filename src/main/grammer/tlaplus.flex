package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

%%

%unicode
%public
%class _TLAplusLexer
%implements FlexLexer
%function advance
%type IElementType
%{
  private int zzNestedModuleLevel = 0;
%}

%state IN_MODULE
%state IN_BLOCK_COMMENT
%state TERMINATED

WHITE_SPACE = " " | \t | \f | \R
SEPARATOR = ---- -*
MODULE_BEGIN = {SEPARATOR} " "* "MODULE"

%%
<YYINITIAL> {
  {MODULE_BEGIN} {
    zzNestedModuleLevel = 0;
    yybegin(IN_MODULE);
    return TLAplusElementTypes.MODULE_BEGIN;
  }
  [^] { return TLAplusElementTypes.IGNORED; }
}

<IN_MODULE> {
  {MODULE_BEGIN} {
    zzNestedModuleLevel++;
    return TLAplusElementTypes.MODULE_BEGIN;
  }

  // keywords
  "ASSUME"     { return TLAplusElementTypes.KEYWORD_ASSUME; }
  "ELSE"       { return TLAplusElementTypes.KEYWORD_ELSE; }
  "LOCAL"      { return TLAplusElementTypes.KEYWORD_LOCAL; }
  "ASSUMPTION" { return TLAplusElementTypes.KEYWORD_ASSUMPTION; }
  "MODULE"     { return TLAplusElementTypes.KEYWORD_MODULE; }
  "VARIABLE"   { return TLAplusElementTypes.KEYWORD_VARIABLE; }
  "VARIABLES"  { return TLAplusElementTypes.KEYWORD_VARIABLES; }
  "AXIOM"      { return TLAplusElementTypes.KEYWORD_AXIOM; }
  "EXCEPT"     { return TLAplusElementTypes.KEYWORD_EXCEPT; }
  "OTHER"      { return TLAplusElementTypes.KEYWORD_OTHER; }
  "CASE"       { return TLAplusElementTypes.KEYWORD_CASE; }
  "EXTENDS"    { return TLAplusElementTypes.KEYWORD_EXTENDS; }
  "SF_"        { return TLAplusElementTypes.KEYWORD_SF_; }
  "WF_"        { return TLAplusElementTypes.KEYWORD_WF_; }
  "CHOOSE"     { return TLAplusElementTypes.KEYWORD_CHOOSE; }
  "IF"         { return TLAplusElementTypes.KEYWORD_IF; }
  "WITH"       { return TLAplusElementTypes.KEYWORD_WITH; }
  "CONSTANTS"  { return TLAplusElementTypes.KEYWORD_CONSTANTS; }
  "CONSTANT"   { return TLAplusElementTypes.KEYWORD_CONSTANT; }
  "IN"         { return TLAplusElementTypes.KEYWORD_IN; }
  "THEN"       { return TLAplusElementTypes.KEYWORD_THEN; }
  "INSTANCE"   { return TLAplusElementTypes.KEYWORD_INSTANCE; }
  "THEOREM"    { return TLAplusElementTypes.KEYWORD_THEOREM; }
  "LET"        { return TLAplusElementTypes.KEYWORD_LET; }

  // quantifier
  \\EE         { return TLAplusElementTypes.QUANTIFIER_EE; }
  \\E          { return TLAplusElementTypes.QUANTIFIER_E; }
  \\AA         { return TLAplusElementTypes.QUANTIFIER_AA; }
  \\A          { return TLAplusElementTypes.QUANTIFIER_A; }

  // literal
  [0-9]+ | [0-9]+ "." [0-9]+ | \\[bB][01]+ | \\[oO][0-7]+ | \\[hH][0-9a-fA-F]+ {
    return TLAplusElementTypes.LITERAL_NUMBER;
  }
  \" ([^\\\"\r\n] | \\[^\r\n])* \" { return TLAplusElementTypes.LITERAL_STRING; }

  // symbols
  "<<"  { return TLAplusElementTypes.LTUPLE; }
  ">>"  { return TLAplusElementTypes.RTUPLE; }
  ">>_" { return TLAplusElementTypes.RTUPLEUNDER; }
  ","   { return TLAplusElementTypes.COMMA; }
  "("   { return TLAplusElementTypes.LPAREN; }
  ")"   { return TLAplusElementTypes.RPAREN; }
  "{"   { return TLAplusElementTypes.LBRACKET; }
  "}"   { return TLAplusElementTypes.RBRACKET; }
  "["   { return TLAplusElementTypes.LSQBRACKET; }
  "]"   { return TLAplusElementTypes.RSQBRACKET; }
  "]_"  { return TLAplusElementTypes.RSQBRACKETUNDER; }
  "."   { return TLAplusElementTypes.DOT; }
  ":"   { return TLAplusElementTypes.COLON; }
  "_"   { return TLAplusElementTypes.UNDER; }
  "@"   { return TLAplusElementTypes.AT; }
  "!"   { return TLAplusElementTypes.BANG; }

  // define
  "==" { return TLAplusElementTypes.DEFINE; }

  // operators
  "ENABLED"            { return TLAplusElementTypes.OP_ENABLED; }
  "UNCHANGED"          { return TLAplusElementTypes.OP_UNCHANGED; }
  "SUBSET"             { return TLAplusElementTypes.OP_POWERSET; }
  "DOMAIN"             { return TLAplusElementTypes.OP_DOMAIN; }
  "~" | \\neg | \\lnot { return TLAplusElementTypes.OP_NEG; }
  "[]"                 { return TLAplusElementTypes.OP_SQUARE; }
  "<>"                 { return TLAplusElementTypes.OP_DIAMOND; }
  -                    { return TLAplusElementTypes.OP_DASH; }
  "/\\" | \\land       { return TLAplusElementTypes.OP_LAND; }
  \\in                 { return TLAplusElementTypes.OP_IN; }
  "<"                  { return TLAplusElementTypes.OP_LT; }
  "=<" | "<=" | \\leq  { return TLAplusElementTypes.OP_LTEQ; }
  \\ll                 { return TLAplusElementTypes.OP_LTLT; }
  \\prec               { return TLAplusElementTypes.OP_PREC; }
  \\preceq             { return TLAplusElementTypes.OP_PRECEQ; }
  \\subseteq           { return TLAplusElementTypes.OP_SUBSETEQ; }
  \\subset             { return TLAplusElementTypes.OP_SUBSET; }
  \\sqsubset           { return TLAplusElementTypes.OP_SQSUBSET; }
  \\sqsubseteq         { return TLAplusElementTypes.OP_SQSUBSETEQ; }
  "|-"                 { return TLAplusElementTypes.OP_VBARDASH; }
  "|="                 { return TLAplusElementTypes.OP_VBAREQ; }
  "->"                 { return TLAplusElementTypes.OP_DASHGT; }
  \\cap | \\intersect  { return TLAplusElementTypes.OP_CAP; }
  \\sqcap              { return TLAplusElementTypes.OP_SQCAP; }
  "(+)" | \\oplus      { return TLAplusElementTypes.OP_OPLUS; }
  "(-)" | \\ominus     { return TLAplusElementTypes.OP_OMINUS; }
  "(.)" | \\odot       { return TLAplusElementTypes.OP_ODOT; }
  "(\\X)" | \\otimes   { return TLAplusElementTypes.OP_OTIMES; }
  "(/)" | \\oslash     { return TLAplusElementTypes.OP_OSLASH; }
  "\\/" | \\lor        { return TLAplusElementTypes.OP_LOR; }
  "<=>" | \\equiv      { return TLAplusElementTypes.OP_EQUIV; }
  \\notin              { return TLAplusElementTypes.OP_NOTIN; }
  ">"                  { return TLAplusElementTypes.OP_GT; }
  ">=" | \\geq         { return TLAplusElementTypes.OP_GTEQ; }
  \\gg                 { return TLAplusElementTypes.OP_GTGT; }
  \\succ               { return TLAplusElementTypes.OP_SUCC; }
  \\succeq             { return TLAplusElementTypes.OP_SUCCEQ; }
  \\supseteq           { return TLAplusElementTypes.OP_SUPSETEQ; }
  \\supset             { return TLAplusElementTypes.OP_SUPSET; }
  \\sqsupset           { return TLAplusElementTypes.OP_SQSUPSET; }
  \\sqsupseteq         { return TLAplusElementTypes.OP_SQSUPSETEQ; }
  "-|"                 { return TLAplusElementTypes.OP_DASHVBAR; }
  "=|"                 { return TLAplusElementTypes.OP_EQVBAR; }
  "<-"                 { return TLAplusElementTypes.OP_LTDASH; }
  \\cup | \\union      { return TLAplusElementTypes.OP_CUP; }
  \\sqcup              { return TLAplusElementTypes.OP_SQCUP; }
  \\uplus              { return TLAplusElementTypes.OP_UPLUS; }
  \\X | \\times        { return TLAplusElementTypes.OP_X; }
  \\wr                 { return TLAplusElementTypes.OP_WR; }
  \\propto             { return TLAplusElementTypes.OP_PROPTO; }
  "=>"                 { return TLAplusElementTypes.OP_EQGT; }
  "="                  { return TLAplusElementTypes.OP_EQ; }
  "/=" | #             { return TLAplusElementTypes.OP_NOTEQ; }
  "->"                 { return TLAplusElementTypes.OP_DASHGT; }
  "-+->"               { return TLAplusElementTypes.OP_DASHPLUSDASHGT; }
  "|->"                { return TLAplusElementTypes.OP_VBARDASHGT; }
  \\div                { return TLAplusElementTypes.OP_DIV; }
  \\cdot               { return TLAplusElementTypes.OP_CDOT; }
  \\circ | \\o         { return TLAplusElementTypes.OP_CIRC; }
  \\bullet             { return TLAplusElementTypes.OP_BULLET; }
  \\star               { return TLAplusElementTypes.OP_STAR; }
  \\bigcirc            { return TLAplusElementTypes.OP_BIGCIRC; }
  \\sim                { return TLAplusElementTypes.OP_SIM; }
  \\simeq              { return TLAplusElementTypes.OP_SIMEQ; }
  \\asymp              { return TLAplusElementTypes.OP_ASYMP; }
  \\approx             { return TLAplusElementTypes.OP_APPROX; }
  \\cong               { return TLAplusElementTypes.OP_CONG; }
  \\doteq              { return TLAplusElementTypes.OP_DOTEQ; }
  "!!"                 { return TLAplusElementTypes.OP_BANGBANG; }
  ##                   { return TLAplusElementTypes.OP_SHARPSHARP; }
  "$"                  { return TLAplusElementTypes.OP_DOLLAR; }
  "$$"                 { return TLAplusElementTypes.OP_DOLLARDOLLAR; }
  %                    { return TLAplusElementTypes.OP_PERCENT; }
  %%                   { return TLAplusElementTypes.OP_PERCENTPERCENT; }
  &                    { return TLAplusElementTypes.OP_AMP; }
  &&                   { return TLAplusElementTypes.OP_AMPAMP; }
  "*"                  { return TLAplusElementTypes.OP_ASTER; }
  "**"                 { return TLAplusElementTypes.OP_ASTERASTER; }
  "+"                  { return TLAplusElementTypes.OP_PLUS; }
  "++"                 { return TLAplusElementTypes.OP_PLUSPLUS; }
  --                   { return TLAplusElementTypes.OP_DASHDASH; }
  ".."                 { return TLAplusElementTypes.OP_DOTDOT; }
  "..."                { return TLAplusElementTypes.OP_DOTDOTDOT; }
  "/"                  { return TLAplusElementTypes.OP_SLASH; }
  "//"                 { return TLAplusElementTypes.OP_SLASHSLASH; }
  ::=                  { return TLAplusElementTypes.OP_COLONCOLONEQ; }
  :=                   { return TLAplusElementTypes.OP_COLONEQ; }
  :>                   { return TLAplusElementTypes.OP_COLONGT; }
  "<:"                 { return TLAplusElementTypes.OP_LTCOLON; }
  "?"                  { return TLAplusElementTypes.OP_QUERY; }
  "??"                 { return TLAplusElementTypes.OP_QUERYQUERY; }
  \\                   { return TLAplusElementTypes.OP_SUBTRACT; }
  "^"                  { return TLAplusElementTypes.OP_CARET; }
  "^^"                 { return TLAplusElementTypes.OP_CARETCARET; }
  "|"                  { return TLAplusElementTypes.OP_VBAR; }
  "||"                 { return TLAplusElementTypes.OP_VBARVBAR; }
  "~>"                 { return TLAplusElementTypes.OP_TILDEGT; }
  \'                   { return TLAplusElementTypes.OP_PRIME; }
  "^+"                 { return TLAplusElementTypes.OP_CARETPLUS; }
  "^*"                 { return TLAplusElementTypes.OP_CARETASTER; }
  "^#"                 { return TLAplusElementTypes.OP_CARETSHARP; }
  "-."                 { return TLAplusElementTypes.OP_DASHDOT; }

  // identifier
  [0-9a-zA-Z_]* [a-zA-Z] [0-9a-zA-Z_]* {
      // fairness operators should be tokenized even if there's no whitespace
      if (yylength() > 3) {
          if ("WF_".equals(yytext().subSequence(0, 3))) {
              yypushback(yylength() - 3);
              return TLAplusElementTypes.KEYWORD_WF_;
          }
          if ("SF_".equals(yytext().subSequence(0, 3))) {
              yypushback(yylength() - 3);
              return TLAplusElementTypes.KEYWORD_SF_;
          }
      }
      return TLAplusElementTypes.IDENTIFIER;
  }

  // comments
  \\"*"[^\r\n]* { return TLAplusElementTypes.COMMENT_LINE; }
  "(*"          { yybegin(IN_BLOCK_COMMENT); yypushback(2); }

  {WHITE_SPACE}+ { return TokenType.WHITE_SPACE; }
  {SEPARATOR}    { return TLAplusElementTypes.SEPARATOR; }
  ==== =*        {
    if (zzNestedModuleLevel == 0) {
        yybegin(TERMINATED);
    } else {
        zzNestedModuleLevel--;
    }
    return TLAplusElementTypes.MODULE_END;
  }
}

<IN_BLOCK_COMMENT> {
  "*)"    { yybegin(IN_MODULE); return TLAplusElementTypes.COMMENT_BLOCK; }
  <<EOF>> { yybegin(YYINITIAL); return TLAplusElementTypes.COMMENT_BLOCK; }
  [^]     {}
}

<TERMINATED> {
  <<EOF>> { yybegin(YYINITIAL); return TLAplusElementTypes.IGNORED; }
  [^]     {}
}

// catch all
[^] { return TokenType.BAD_CHARACTER; }
