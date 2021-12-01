package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusBraceMatcher implements PairedBraceMatcher {
    private static final TokenSet ALLOWED_BEFORE_TOKENS = TokenSet.create(
            TLAplusElementTypes.COMMENT,
            TLAplusElementTypes.COMMA,
            TLAplusElementTypes.SEMICOLON,
            TLAplusElementTypes.RBRACKET,
            TLAplusElementTypes.RSQBRACKET,
            TLAplusElementTypes.RTUPLE,
            TLAplusElementTypes.RPAREN,
            TokenType.WHITE_SPACE
    );

    @Override
    public BracePair @NotNull [] getPairs() {
        return new BracePair[]{
                new BracePair(TLAplusElementTypes.LBRACKET, TLAplusElementTypes.RBRACKET, true),
                new BracePair(TLAplusElementTypes.LSQBRACKET, TLAplusElementTypes.RSQBRACKET, false),
                new BracePair(TLAplusElementTypes.LTUPLE, TLAplusElementTypes.RTUPLE, false),
                new BracePair(TLAplusElementTypes.LPAREN, TLAplusElementTypes.RPAREN, false)
        };
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(
            @NotNull IElementType lbraceType,
            @Nullable IElementType contextType) {
        return ALLOWED_BEFORE_TOKENS.contains(contextType);
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
