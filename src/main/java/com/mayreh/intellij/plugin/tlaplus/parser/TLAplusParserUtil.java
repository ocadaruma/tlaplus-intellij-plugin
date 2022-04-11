package com.mayreh.intellij.plugin.tlaplus.parser;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.JUNCTION_BEGIN;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.JUNCTION_BREAK;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.JUNCTION_CONT;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.OP_LAND2;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.OP_LOR2;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

public class TLAplusParserUtil extends GeneratedParserUtilBase {
    public static boolean parseInfixOpLor(PsiBuilder b, int level) {
        return parseInfixOpLandLor(OP_LOR2, b, level);
    }

    public static boolean parseInfixOpLand(PsiBuilder b, int level) {
        return parseInfixOpLandLor(OP_LAND2, b, level);
    }

    /*
     * Special treatment for /\ or \/ which parsed as infix-op rather than junction-list.
     * TLAplusLexer generates JUNCTION_BEGIN, JUNCTION_BREAK tokens for /\ or \/ based on indentation
     * even if it should be parsed as infix-op. (Because infix-op or junction-list cannot be determined in lexing-phase)
     *
     * It breaks junction-list parsing in some cases. Let's take following expression as an example.
     *   X ==
     *      /\ /\ A \/ B
     *         /\ C
     *      /\ D
     *
     * A \/ B is an infix-op-expr, but the lexer generates
     *   A JUNCTION_BEGIN \/ B JUNCTION_BREAK JUNCTION_CONT /\ C
     * Hence outer junction-list (/\) arm cannot be parsed correctly as /\ C.
     *
     * To address that, we remap "false" JUNCTION_BEGIN, JUNCTION_BREAK for \/ to WHITE_SPACE to be
     * ignored by parser if \/ is parsed as infix-op.
     */
    private static boolean parseInfixOpLandLor(
            IElementType opToken,
            PsiBuilder b, int level) {
        if (!recursion_guard_(b, level, "infix_op_land_lor")) {
            return false;
        }
        if (!nextTokenIs(b, "<infix op land lor>", JUNCTION_BEGIN, opToken)) {
            return false;
        }

        Marker marker = enter_section_(b, level, _NONE_, "<infix op land lor>");

        final boolean falseJunctionBegin;
        boolean result = false;
        if (b.getTokenType() == JUNCTION_BEGIN) {
            falseJunctionBegin = true;
            if (b.lookAhead(1) == opToken) {
                result = true;
            }
        } else {
            falseJunctionBegin = false;
            result = true;
        }

        if (falseJunctionBegin) {
            if (result) {
                b.remapCurrentToken(TokenType.WHITE_SPACE);
            }
            b.advanceLexer();
        }
        b.advanceLexer();
        exit_section_(b, level, marker, result, false, null);

        Marker pos = b.mark();
        if (result && falseJunctionBegin) {
            while (!b.eof()) {
                b.advanceLexer();
                if (b.getTokenType() == JUNCTION_CONT) {
                    b.remapCurrentToken(TokenType.WHITE_SPACE);
                }
                if (b.getTokenType() == JUNCTION_BREAK) {
                    b.remapCurrentToken(TokenType.WHITE_SPACE);
                    break;
                }
            }
        }
        pos.rollbackTo();
        return result;
    }
}
