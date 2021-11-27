package com.mayreh.intellij.plugin.tlaplus.psi;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.PLUS_CAL_ALGORITHM;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.mayreh.intellij.plugin.pluscal.PlusCalParserDefinition;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;
import com.mayreh.intellij.plugin.tlaplus.parser.TLAplusParser;

/**
 * Represents an AST node of TLA+, parsed by {@link TLAplusParser}.
 */
public class TLAplusElementType extends IElementType {
    public TLAplusElementType(String debugName) {
        super(debugName, TLAplusLanguage.INSTANCE);
    }

    public static final PlusCalCommentElementType COMMENT_PLUS_CAL = new PlusCalCommentElementType();
    public static class PlusCalCommentElementType extends ILazyParseableElementType {
        private static final PlusCalParserDefinition PARSER_DEFINITION = new PlusCalParserDefinition();

        private PlusCalCommentElementType() {
            super("COMMENT_PLUS_CAL", TLAplusLanguage.INSTANCE);
        }

        @Override
        protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
            PsiParser parser = PARSER_DEFINITION.createParser(psi.getProject());
            Lexer lexer = PARSER_DEFINITION.createLexer(psi.getProject());
            PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(
                    PARSER_DEFINITION, lexer, chameleon.getText());
            return parser.parse(PLUS_CAL_ALGORITHM, builder);
        }

        @Override
        public @Nullable ASTNode createNode(CharSequence text) {
            return new PlusCalCommentImpl(this, text);
        }
    }

    public static class PlusCalCommentImpl extends LazyParseablePsiElement {
        public PlusCalCommentImpl(@NotNull IElementType type, @Nullable CharSequence buffer) {
            super(type, buffer);
        }
    }
}
