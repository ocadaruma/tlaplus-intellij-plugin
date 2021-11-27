package com.mayreh.intellij.plugin.pluscal.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusFlexLexerBase;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

%%

%unicode
%public
%class _PlusCalLexer
%extends TLAplusFlexLexerBase
%function advance
%type IElementType
%{
  private int zzNestedBlockCommentLevel = 0;

  public _PlusCalLexer(boolean forHighlighting) {
      this(null);
      setForHighlighting(forHighlighting);
  }

  @Override
  public int stateHandleIndent() {
      return HANDLE_INDENT;
  }

  @Override
  public int stateDefault() {
      return YYINITIAL;
  }

  @Override
  public CharSequence zzBuffer() {
      return zzBuffer;
  }

  @Override
  public int zzCurrentPos() {
      return zzCurrentPos;
  }
%}

%state HANDLE_INDENT
%state IN_BLOCK_COMMENT

WHITE_SPACE = " " | \t | \f | \R
IDENTIFIER = [0-9a-zA-Z_]* [a-zA-Z] [0-9a-zA-Z_]*

%%
<YYINITIAL, HANDLE_INDENT> {
  --algorithm | (--fair {WHITE_SPACE} algorithm) {
    return TLAplusElementTypes.PLUS_CAL_ALGORITHM_BEGIN;
  }

  // pluscal keywords
  "algorithm"  { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_ALGORITHM); }
  "assert"     { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_ASSERT); }
  "await"      { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_AWAIT); }
  "begin"      { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_BEGIN); }
  "call"       { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_CALL); }
  "define"     { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_DEFINE); }
  "do"         { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_DO); }
  "either"     { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_EITHER); }
  "else"       { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_ELSE); }
  "elsif"      { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_ELSIF); }
  "end"        { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_END); }
  "goto"       { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_GOTO); }
  "if"         { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_IF); }
  "macro"      { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_MACRO); }
  "or"         { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_OR); }
  "print"      { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_PRINT); }
  "procedure"  { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_PROCEDURE); }
  "process"    { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_PROCESS); }
  "return"     { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_RETURN); }
  "skip"       { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_SKIP); }
  "then"       { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_THEN); }
  "variable"   { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_VARIABLE); }
  "variables"  { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_VARIABLES); }
  "when"       { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_WHEN); }
  "while"      { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_WHILE); }
  "with"       { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_WITH); }
  "fair"       { return maybeHandleIndent(TLAplusElementTypes.PLUS_CAL_KW_FAIR); }

  // keywords
  "ASSUME"     { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_ASSUME); }
  "ELSE"       { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_ELSE); }
  "LOCAL"      { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_LOCAL); }
  "ASSUMPTION" { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_ASSUMPTION); }
  "MODULE"     { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_MODULE); }
  "VARIABLE"   { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_VARIABLE); }
  "VARIABLES"  { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_VARIABLES); }
  "AXIOM"      { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_AXIOM); }
  "EXCEPT"     { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_EXCEPT); }
  "OTHER"      { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_OTHER); }
  "CASE"       { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_CASE); }
  "EXTENDS"    { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_EXTENDS); }
  "SF_"        { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_SF_); }
  "WF_"        { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_WF_); }
  "CHOOSE"     { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_CHOOSE); }
  "IF"         { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_IF); }
  "WITH"       { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_WITH); }
  "CONSTANTS"  { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_CONSTANTS); }
  "CONSTANT"   { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_CONSTANT); }
  "IN"         { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_IN); }
  "THEN"       { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_THEN); }
  "INSTANCE"   { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_INSTANCE); }
  "THEOREM"    { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_THEOREM); }
  "LET"        { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_LET); }
  "LAMBDA"     { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_LAMBDA); }

  // quantifier
  \\EE         { return maybeHandleIndent(TLAplusElementTypes.QUANTIFIER_EE); }
  \\E          { return maybeHandleIndent(TLAplusElementTypes.QUANTIFIER_E); }
  \\AA         { return maybeHandleIndent(TLAplusElementTypes.QUANTIFIER_AA); }
  \\A          { return maybeHandleIndent(TLAplusElementTypes.QUANTIFIER_A); }

  // literal
  [0-9]+ | [0-9]+ "." [0-9]+ | \\[bB][01]+ | \\[oO][0-7]+ | \\[hH][0-9a-fA-F]+ {
    return maybeHandleIndent(TLAplusElementTypes.LITERAL_NUMBER);
  }
  \" ([^\\\"\r\n\t] | \\[^\r\n\t])* \" { return maybeHandleIndent(TLAplusElementTypes.LITERAL_STRING); }

  // symbols
  "<<"  { return maybeHandleIndent(TLAplusElementTypes.LTUPLE); }
  ">>"  { return maybeHandleIndent(TLAplusElementTypes.RTUPLE); }
  ">>_" { return maybeHandleIndent(TLAplusElementTypes.RTUPLEUNDER); }
  ","   { return maybeHandleIndent(TLAplusElementTypes.COMMA); }
  "("   { return maybeHandleIndent(TLAplusElementTypes.LPAREN); }
  ")"   { return maybeHandleIndent(TLAplusElementTypes.RPAREN); }
  "{"   { return maybeHandleIndent(TLAplusElementTypes.LBRACKET); }
  "}"   { return maybeHandleIndent(TLAplusElementTypes.RBRACKET); }
  "["   { return maybeHandleIndent(TLAplusElementTypes.LSQBRACKET); }
  "]"   { return maybeHandleIndent(TLAplusElementTypes.RSQBRACKET); }
  "]_"  { return maybeHandleIndent(TLAplusElementTypes.RSQBRACKETUNDER); }
  "."   { return maybeHandleIndent(TLAplusElementTypes.DOT); }
  ":"   { return maybeHandleIndent(TLAplusElementTypes.COLON); }
  ";"   { return maybeHandleIndent(TLAplusElementTypes.SEMICOLON); }
  "_"   { return maybeHandleIndent(TLAplusElementTypes.UNDER); }
  "@"   { return maybeHandleIndent(TLAplusElementTypes.AT); }
  "!"   { return maybeHandleIndent(TLAplusElementTypes.BANG); }

  // define
  "==" { return maybeHandleIndent(TLAplusElementTypes.DEFINE); }

  // operators
  "ENABLED"            { return maybeHandleIndent(TLAplusElementTypes.OP_ENABLED); }
  "UNCHANGED"          { return maybeHandleIndent(TLAplusElementTypes.OP_UNCHANGED); }
  "SUBSET"             { return maybeHandleIndent(TLAplusElementTypes.OP_POWERSET); }
  "DOMAIN"             { return maybeHandleIndent(TLAplusElementTypes.OP_DOMAIN); }
  "~" | \\neg | \\lnot { return maybeHandleIndent(TLAplusElementTypes.OP_NEG); }
  "[]"                 { return maybeHandleIndent(TLAplusElementTypes.OP_SQUARE); }
  "<>"                 { return maybeHandleIndent(TLAplusElementTypes.OP_DIAMOND); }
  -                    { return maybeHandleIndent(TLAplusElementTypes.OP_DASH); }
  "/\\"                { return maybeJunction(TLAplusElementTypes.OP_LAND2); }
  \\land               { return maybeHandleIndent(TLAplusElementTypes.OP_LAND); }
  "\\/"                { return maybeJunction(TLAplusElementTypes.OP_LOR2); }
  \\lor                { return maybeHandleIndent(TLAplusElementTypes.OP_LOR); }
  \\in                 { return maybeHandleIndent(TLAplusElementTypes.OP_IN); }
  "<"                  { return maybeHandleIndent(TLAplusElementTypes.OP_LT); }
  "=<" | "<=" | \\leq  { return maybeHandleIndent(TLAplusElementTypes.OP_LTEQ); }
  \\ll                 { return maybeHandleIndent(TLAplusElementTypes.OP_LTLT); }
  \\prec               { return maybeHandleIndent(TLAplusElementTypes.OP_PREC); }
  \\preceq             { return maybeHandleIndent(TLAplusElementTypes.OP_PRECEQ); }
  \\subseteq           { return maybeHandleIndent(TLAplusElementTypes.OP_SUBSETEQ); }
  \\subset             { return maybeHandleIndent(TLAplusElementTypes.OP_SUBSET); }
  \\sqsubset           { return maybeHandleIndent(TLAplusElementTypes.OP_SQSUBSET); }
  \\sqsubseteq         { return maybeHandleIndent(TLAplusElementTypes.OP_SQSUBSETEQ); }
  "|-"                 { return maybeHandleIndent(TLAplusElementTypes.OP_VBARDASH); }
  "|="                 { return maybeHandleIndent(TLAplusElementTypes.OP_VBAREQ); }
  "->"                 { return maybeHandleIndent(TLAplusElementTypes.OP_DASHGT); }
  \\cap | \\intersect  { return maybeHandleIndent(TLAplusElementTypes.OP_CAP); }
  \\sqcap              { return maybeHandleIndent(TLAplusElementTypes.OP_SQCAP); }
  "(+)" | \\oplus      { return maybeHandleIndent(TLAplusElementTypes.OP_OPLUS); }
  "(-)" | \\ominus     { return maybeHandleIndent(TLAplusElementTypes.OP_OMINUS); }
  "(.)" | \\odot       { return maybeHandleIndent(TLAplusElementTypes.OP_ODOT); }
  "(\\X)" | \\otimes   { return maybeHandleIndent(TLAplusElementTypes.OP_OTIMES); }
  "(/)" | \\oslash     { return maybeHandleIndent(TLAplusElementTypes.OP_OSLASH); }
  "<=>" | \\equiv      { return maybeHandleIndent(TLAplusElementTypes.OP_EQUIV); }
  \\notin              { return maybeHandleIndent(TLAplusElementTypes.OP_NOTIN); }
  ">"                  { return maybeHandleIndent(TLAplusElementTypes.OP_GT); }
  ">=" | \\geq         { return maybeHandleIndent(TLAplusElementTypes.OP_GTEQ); }
  \\gg                 { return maybeHandleIndent(TLAplusElementTypes.OP_GTGT); }
  \\succ               { return maybeHandleIndent(TLAplusElementTypes.OP_SUCC); }
  \\succeq             { return maybeHandleIndent(TLAplusElementTypes.OP_SUCCEQ); }
  \\supseteq           { return maybeHandleIndent(TLAplusElementTypes.OP_SUPSETEQ); }
  \\supset             { return maybeHandleIndent(TLAplusElementTypes.OP_SUPSET); }
  \\sqsupset           { return maybeHandleIndent(TLAplusElementTypes.OP_SQSUPSET); }
  \\sqsupseteq         { return maybeHandleIndent(TLAplusElementTypes.OP_SQSUPSETEQ); }
  "-|"                 { return maybeHandleIndent(TLAplusElementTypes.OP_DASHVBAR); }
  "=|"                 { return maybeHandleIndent(TLAplusElementTypes.OP_EQVBAR); }
  "<-"                 { return maybeHandleIndent(TLAplusElementTypes.OP_LTDASH); }
  \\cup | \\union      { return maybeHandleIndent(TLAplusElementTypes.OP_CUP); }
  \\sqcup              { return maybeHandleIndent(TLAplusElementTypes.OP_SQCUP); }
  \\uplus              { return maybeHandleIndent(TLAplusElementTypes.OP_UPLUS); }
  \\X | \\times        { return maybeHandleIndent(TLAplusElementTypes.OP_X); }
  \\wr                 { return maybeHandleIndent(TLAplusElementTypes.OP_WR); }
  \\propto             { return maybeHandleIndent(TLAplusElementTypes.OP_PROPTO); }
  "=>"                 { return maybeHandleIndent(TLAplusElementTypes.OP_EQGT); }
  "="                  { return maybeHandleIndent(TLAplusElementTypes.OP_EQ); }
  "/=" | #             { return maybeHandleIndent(TLAplusElementTypes.OP_NOTEQ); }
  "->"                 { return maybeHandleIndent(TLAplusElementTypes.OP_DASHGT); }
  "-+->"               { return maybeHandleIndent(TLAplusElementTypes.OP_DASHPLUSDASHGT); }
  "|->"                { return maybeHandleIndent(TLAplusElementTypes.OP_VBARDASHGT); }
  \\div                { return maybeHandleIndent(TLAplusElementTypes.OP_DIV); }
  \\cdot               { return maybeHandleIndent(TLAplusElementTypes.OP_CDOT); }
  \\circ | \\o         { return maybeHandleIndent(TLAplusElementTypes.OP_CIRC); }
  \\bullet             { return maybeHandleIndent(TLAplusElementTypes.OP_BULLET); }
  \\star               { return maybeHandleIndent(TLAplusElementTypes.OP_STAR); }
  \\bigcirc            { return maybeHandleIndent(TLAplusElementTypes.OP_BIGCIRC); }
  \\sim                { return maybeHandleIndent(TLAplusElementTypes.OP_SIM); }
  \\simeq              { return maybeHandleIndent(TLAplusElementTypes.OP_SIMEQ); }
  \\asymp              { return maybeHandleIndent(TLAplusElementTypes.OP_ASYMP); }
  \\approx             { return maybeHandleIndent(TLAplusElementTypes.OP_APPROX); }
  \\cong               { return maybeHandleIndent(TLAplusElementTypes.OP_CONG); }
  \\doteq              { return maybeHandleIndent(TLAplusElementTypes.OP_DOTEQ); }
  "!!"                 { return maybeHandleIndent(TLAplusElementTypes.OP_BANGBANG); }
  ##                   { return maybeHandleIndent(TLAplusElementTypes.OP_SHARPSHARP); }
  "$"                  { return maybeHandleIndent(TLAplusElementTypes.OP_DOLLAR); }
  "$$"                 { return maybeHandleIndent(TLAplusElementTypes.OP_DOLLARDOLLAR); }
  %                    { return maybeHandleIndent(TLAplusElementTypes.OP_PERCENT); }
  %%                   { return maybeHandleIndent(TLAplusElementTypes.OP_PERCENTPERCENT); }
  &                    { return maybeHandleIndent(TLAplusElementTypes.OP_AMP); }
  &&                   { return maybeHandleIndent(TLAplusElementTypes.OP_AMPAMP); }
  "*"                  { return maybeHandleIndent(TLAplusElementTypes.OP_ASTER); }
  "**"                 { return maybeHandleIndent(TLAplusElementTypes.OP_ASTERASTER); }
  "+"                  { return maybeHandleIndent(TLAplusElementTypes.OP_PLUS); }
  "++"                 { return maybeHandleIndent(TLAplusElementTypes.OP_PLUSPLUS); }
  --                   { return maybeHandleIndent(TLAplusElementTypes.OP_DASHDASH); }
  ".."                 { return maybeHandleIndent(TLAplusElementTypes.OP_DOTDOT); }
  "..."                { return maybeHandleIndent(TLAplusElementTypes.OP_DOTDOTDOT); }
  "/"                  { return maybeHandleIndent(TLAplusElementTypes.OP_SLASH); }
  "//"                 { return maybeHandleIndent(TLAplusElementTypes.OP_SLASHSLASH); }
  ::=                  { return maybeHandleIndent(TLAplusElementTypes.OP_COLONCOLONEQ); }
  :=                   { return maybeHandleIndent(TLAplusElementTypes.OP_COLONEQ); }
  :>                   { return maybeHandleIndent(TLAplusElementTypes.OP_COLONGT); }
  "<:"                 { return maybeHandleIndent(TLAplusElementTypes.OP_LTCOLON); }
  "?"                  { return maybeHandleIndent(TLAplusElementTypes.OP_QUERY); }
  "??"                 { return maybeHandleIndent(TLAplusElementTypes.OP_QUERYQUERY); }
  \\                   { return maybeHandleIndent(TLAplusElementTypes.OP_SUBTRACT); }
  "^"                  { return maybeHandleIndent(TLAplusElementTypes.OP_CARET); }
  "^^"                 { return maybeHandleIndent(TLAplusElementTypes.OP_CARETCARET); }
  "|"                  { return maybeHandleIndent(TLAplusElementTypes.OP_VBAR); }
  "||"                 { return maybeHandleIndent(TLAplusElementTypes.OP_VBARVBAR); }
  "@@"                 { return maybeHandleIndent(TLAplusElementTypes.OP_ATAT); }
  "~>"                 { return maybeHandleIndent(TLAplusElementTypes.OP_TILDEGT); }
  \'                   { return maybeHandleIndent(TLAplusElementTypes.OP_PRIME); }
  "^+"                 { return maybeHandleIndent(TLAplusElementTypes.OP_CARETPLUS); }
  "^*"                 { return maybeHandleIndent(TLAplusElementTypes.OP_CARETASTER); }
  "^#"                 { return maybeHandleIndent(TLAplusElementTypes.OP_CARETSHARP); }
  "-."                 { return maybeHandleIndent(TLAplusElementTypes.OP_DASHDOT); }

  WF_ / {IDENTIFIER} { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_WF_); }

  SF_ / {IDENTIFIER} { return maybeHandleIndent(TLAplusElementTypes.KEYWORD_SF_); }

  {IDENTIFIER} { return maybeHandleIndent(TLAplusElementTypes.IDENTIFIER); }

  // comments
  \\"*"[^\r\n]* { return TLAplusElementTypes.COMMENT; }
  "(*"          { zzNestedBlockCommentLevel = 0; yybegin(IN_BLOCK_COMMENT); yypushback(2); }

  {WHITE_SPACE}+ { return TokenType.WHITE_SPACE; }
}

<IN_BLOCK_COMMENT> {
  "(*"    { zzNestedBlockCommentLevel++; }
  "*)"    {
      zzNestedBlockCommentLevel--;
      if (zzNestedBlockCommentLevel == 0) {
          yybegin(YYINITIAL);
          return TLAplusElementTypes.COMMENT;
      }
  }
  <<EOF>> { yybegin(YYINITIAL); return TLAplusElementTypes.COMMENT; }
  [^]     {}
}

// catch all
[^] { return TokenType.BAD_CHARACTER; }
