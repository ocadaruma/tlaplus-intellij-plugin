package com.mayreh.intellij.plugin.fragment;

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
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;
import com.mayreh.intellij.plugin.tlaplus.TLAplusParserDefinition;
import com.mayreh.intellij.plugin.tlaplus.lexer.TLAplusLexer;
import com.mayreh.intellij.plugin.tlaplus.lexer._TLAplusCodeFragmentLexer;
import com.mayreh.intellij.plugin.tlaplus.parser.TLAplusParser;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLAplusCodeFragmentParserDefinition extends TLAplusParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(TLAplusCodeFragmentLanguage.INSTANCE);
    public static class CodeFragmentLexer extends MergingLexerAdapter {
        public CodeFragmentLexer(boolean forHighlighting) {
            super(new FlexAdapter(new _TLAplusCodeFragmentLexer(forHighlighting)),
                  TokenSet.create(TLAplusElementTypes.COMMENT));
        }
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new CodeFragmentLexer(false);
    }

    @Override
    public PsiParser createParser(Project project) {
        return new TLAplusParser() {
            @Override
            protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
                return super.parse_root_(TLAplusElementTypes.CODE_FRAGMENT, builder_);
            }
        };
    }
}
