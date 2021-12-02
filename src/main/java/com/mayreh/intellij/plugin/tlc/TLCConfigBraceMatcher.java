package com.mayreh.intellij.plugin.tlc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlc.psi.TLCConfigElementTypes;

public class TLCConfigBraceMatcher implements PairedBraceMatcher {
    private static final TokenSet ALLOWED_BEFORE_TOKENS = TokenSet.create(
            TLCConfigElementTypes.COMMENT,
            TLCConfigElementTypes.COMMA,
            TLCConfigElementTypes.RBRACE,
            TokenType.WHITE_SPACE
    );

    @Override
    public BracePair @NotNull [] getPairs() {
        return new BracePair[]{
                new BracePair(TLCConfigElementTypes.LBRACE, TLCConfigElementTypes.RBRACE, true)
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
