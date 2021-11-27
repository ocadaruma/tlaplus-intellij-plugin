package com.mayreh.intellij.plugin.pluscal;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.pluscal.lexer._PlusCalLexer;
import com.mayreh.intellij.plugin.tlaplus.parser.TLAplusParser;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementType;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class PlusCalParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(PlusCalLanguage.INSTANCE);
    public static final TokenSet COMMENT_TOKENS = TokenSet.create(
            TLAplusElementTypes.COMMENT,
            TLAplusElementType.COMMENT_PLUS_CAL);
    public static final TokenSet STRING_LITERAL = TokenSet.create(TLAplusElementTypes.LITERAL_STRING);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new MergingLexerAdapter(new FlexAdapter(new _PlusCalLexer(false)),
                                       TokenSet.create(TLAplusElementTypes.COMMENT));
    }

    @Override
    public PsiParser createParser(Project project) {
        return new TLAplusParser() {
            @Override
            protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
                return super.parse_root_(TLAplusElementTypes.PLUS_CAL_ALGORITHM, builder_);
            }
        };
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
        return new PlusCalFile(viewProvider);
    }
}
