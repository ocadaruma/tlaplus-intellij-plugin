package com.mayreh.intellij.plugin.pluscal;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.TLAplusParserDefinition;
import com.mayreh.intellij.plugin.tlaplus.parser.TLAplusParser;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class PlusCalParserDefinition extends TLAplusParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(PlusCalLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new PlusCalLexer(false);
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
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new TLAplusFile(viewProvider, PlusCalLanguage.INSTANCE);
    }
}
