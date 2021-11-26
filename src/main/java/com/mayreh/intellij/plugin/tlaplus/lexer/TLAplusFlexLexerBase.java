package com.mayreh.intellij.plugin.tlaplus.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.Stack;
import com.mayreh.intellij.plugin.tlaplus.lexer.JunctionIndentation.Type;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public abstract class TLAplusFlexLexerBase implements FlexLexer {
    private final Stack<JunctionIndentation> zzIndentationStack = new Stack<>(1000);
    private boolean forHighlighting = false;

    protected void setForHighlighting(boolean forHighlighting) {
        this.forHighlighting = forHighlighting;
    }

    protected abstract void yypushback(int number);
    protected abstract int yylength();
    protected abstract int stateHandleIndent();
    protected abstract int stateDefault();
    protected abstract CharSequence zzBuffer();
    protected abstract int zzCurrentPos();

    protected IElementType clearIndent(IElementType e, int nextState) {
        if (forHighlighting) {
            if (yystate() != nextState) {
                yybegin(nextState);
            }
            return e;
        }

        JunctionIndentation i = zzIndentationStack.isEmpty() ? null : zzIndentationStack.peek();
        if (i == null) {
            yybegin(nextState);
            return e;
        }
        zzIndentationStack.pop();
        yypushback(yylength());
        yybegin(stateHandleIndent());
        return TLAplusElementTypes.JUNCTION_BREAK;
    }

    protected IElementType maybeHandleIndent(IElementType e) {
        if (forHighlighting) {
            return e;
        }

        JunctionIndentation i = zzIndentationStack.isEmpty() ? null : zzIndentationStack.peek();
        if (i == null) {
            if (yystate() == stateHandleIndent()) {
                yybegin(stateDefault());
            }
            return e;
        }
        int column = StringUtil.offsetToLineColumn(zzBuffer(), zzCurrentPos()).column;
        if (i.column() < column) {
            if (yystate() == stateHandleIndent()) {
                yybegin(stateDefault());
            }
            return e;
        }
        zzIndentationStack.pop();
        yypushback(yylength());
        yybegin(stateHandleIndent());
        return TLAplusElementTypes.JUNCTION_BREAK;
    }

    protected IElementType maybeJunction(IElementType operator) {
        if (forHighlighting) {
            return operator;
        }
        if (yystate() == stateHandleIndent()) {
            yybegin(stateDefault());
            return operator;
        }
        JunctionIndentation i = zzIndentationStack.isEmpty() ? null : zzIndentationStack.peek();
        int column = StringUtil.offsetToLineColumn(zzBuffer(), zzCurrentPos()).column;
        if (i == null || i.column() < column) {
            zzIndentationStack.push(JunctionIndentation.and(column));
            yybegin(stateHandleIndent());
            yypushback(yylength());
            return TLAplusElementTypes.JUNCTION_BEGIN;
        }
        if (i.type() == Type.And && i.column() == column) {
            yybegin(stateHandleIndent());
            yypushback(yylength());
            return TLAplusElementTypes.JUNCTION_CONT;
        }

        zzIndentationStack.pop();
        yypushback(yylength());
        return TLAplusElementTypes.JUNCTION_BREAK;
    }
}
