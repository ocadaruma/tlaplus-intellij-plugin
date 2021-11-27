package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

%%

%unicode
%public
%class _TLAplusPlusCalCommentLexer
%implements FlexLexer
%function advance
%type IElementType
%{
  private int cSyntaxBraceNestLevel = 0;
  private int zzNestedBlockCommentLevel = 0;
%}

%state ALGORITHM_BEGIN
%state P_SYNTAX
%state C_SYNTAX
%state IN_BLOCK_COMMENT_C
%state IN_BLOCK_COMMENT_P
%state TERMINATED

WHITE_SPACE = " " | \t | \f | \R
IDENTIFIER = [0-9a-zA-Z_]* [a-zA-Z] [0-9a-zA-Z_]*

%%
<YYINITIAL> {
  --fair | --algorithm {
      yypushback(yylength());
      yybegin(ALGORITHM_BEGIN);
  }
  [^] { return TLAplusElementTypes.IGNORED; }
}
<ALGORITHM_BEGIN> {
  --fair {WHITE_SPACE} algorithm {WHITE_SPACE} {IDENTIFIER} {WHITE_SPACE}? "{" {
      cSyntaxBraceNestLevel = 1;
      yybegin(C_SYNTAX);
  }
  --algorithm {WHITE_SPACE} {IDENTIFIER} {WHITE_SPACE}? "{" {
      cSyntaxBraceNestLevel = 1;
      yybegin(C_SYNTAX);
  }
  --fair {WHITE_SPACE} algorithm {WHITE_SPACE} {IDENTIFIER} {WHITE_SPACE} {
      yybegin(P_SYNTAX);
  }
  --algorithm {WHITE_SPACE} {IDENTIFIER} {WHITE_SPACE} {
      yybegin(P_SYNTAX);
  }
  <<EOF>> { yybegin(TERMINATED); return TLAplusElementTypes.COMMENT_PLUS_CAL; }
  [^] {}
}

<C_SYNTAX> {
  \\"*"[^\r\n]* {}
  "(*"          { zzNestedBlockCommentLevel = 0; yybegin(IN_BLOCK_COMMENT_C); yypushback(2); }
  "{"           { cSyntaxBraceNestLevel++; }
  "}"           {
     cSyntaxBraceNestLevel--;
     if (cSyntaxBraceNestLevel == 0) {
         yybegin(TERMINATED);
         return TLAplusElementTypes.COMMENT_PLUS_CAL;
     }
  }
  <<EOF>> { yybegin(TERMINATED); return TLAplusElementTypes.COMMENT_PLUS_CAL; }
  [^] {}
}

<P_SYNTAX> {
  \\"*"[^\r\n]* {}
  "(*"          { zzNestedBlockCommentLevel = 0; yybegin(IN_BLOCK_COMMENT_P); yypushback(2); }
  end {WHITE_SPACE} algorithm {
      yybegin(TERMINATED);
      return TLAplusElementTypes.COMMENT_PLUS_CAL;
  }
  <<EOF>> { yybegin(TERMINATED); return TLAplusElementTypes.COMMENT_PLUS_CAL; }
  [^] {}
}

<IN_BLOCK_COMMENT_C> {
  "(*"    { zzNestedBlockCommentLevel++; }
  "*)"    {
      zzNestedBlockCommentLevel--;
      if (zzNestedBlockCommentLevel == 0) {
          yybegin(C_SYNTAX);
      }
  }
  <<EOF>> { yybegin(TERMINATED); return TLAplusElementTypes.COMMENT_PLUS_CAL; }
  [^]     {}
}

<IN_BLOCK_COMMENT_P> {
  "(*"    { zzNestedBlockCommentLevel++; }
  "*)"    {
      zzNestedBlockCommentLevel--;
      if (zzNestedBlockCommentLevel == 0) {
          yybegin(P_SYNTAX);
      }
  }
  <<EOF>> { yybegin(TERMINATED); return TLAplusElementTypes.COMMENT_PLUS_CAL; }
  [^]     {}
}

<TERMINATED> {
  [^]     { return TLAplusElementTypes.IGNORED; }
}

// catch all
[^] { return TokenType.BAD_CHARACTER; }
