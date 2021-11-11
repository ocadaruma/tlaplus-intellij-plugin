package com.mayreh.intellij.plugin.tlc;

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
import com.mayreh.intellij.plugin.tlc.lexer.TLCConfigLexer;
import com.mayreh.intellij.plugin.tlc.parser.TLCConfigParser;
import com.mayreh.intellij.plugin.tlc.psi.TLCConfigElementTypes;

public class TLCConfigParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(TLCConfigLanguage.INSTANCE);
    public static final TokenSet COMMENT_TOKENS = TokenSet.create(
            TLCConfigElementTypes.COMMENT_LINE,
            TLCConfigElementTypes.COMMENT_BLOCK);
    public static final TokenSet STRING_LITERAL = TokenSet.create(TLCConfigElementTypes.LITERAL_STRING);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new TLCConfigLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new TLCConfigParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
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
    public @NotNull PsiElement createElement(ASTNode node) {
        return TLCConfigElementTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new TLCConfigFile(viewProvider);
    }
}
