package com.mayreh.intellij.plugin.tlaplus;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusLexer;
import com.mayreh.intellij.plugin.tlaplus.parser.TLAplusParser;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(TLAplusLanguage.INSTANCE);
    public static final TokenSet COMMENT_TOKENS = TokenSet.create(
            TLAplusElementTypes.COMMENT_LINE,
            TLAplusElementTypes.COMMENT_BLOCK,
            TLAplusElementTypes.IGNORED);
    public static final TokenSet STRING_LITERAL = TokenSet.create(TLAplusElementTypes.LITERAL_STRING);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new TLAplusLexer(false);
    }

    @Override
    public PsiParser createParser(Project project) {
        return new TLAplusParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return COMMENT_TOKENS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return STRING_LITERAL;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.WHITE_SPACE;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return TLAplusElementTypes.Factory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new TLAplusFile(viewProvider);
    }
}
