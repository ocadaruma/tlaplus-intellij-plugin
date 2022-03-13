package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInfixOp;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInfixOpName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNameFixness;

public abstract class TLAplusInfixOpNameImplMixin extends TLAplusNamedElementImplBase implements TLAplusInfixOpName {
    private static final String[] LTEQ = {"\\leq", "=<", "<="};
    private static final String[] CAP = {"\\cap", "\\intersect"};
    private static final String[] OPLUS = {"\\oplus", "(+)"};
    private static final String[] OMINUS = {"\\ominus", "(-)"};
    private static final String[] ODOT = {"\\odot", "(.)"};
    private static final String[] OTIMES = {"\\otimes", "(\\X)"};
    private static final String[] OSLASH = {"\\oslash", "(/)"};
    private static final String[] GTEQ = {"\\geq", ">="};
    private static final String[] CUP = {"\\cup", "\\union"};
    private static final String[] NOTEQ = {"/=", "#"};
    private static final String[] CIRC = {"\\circ", "\\o"};

    protected TLAplusInfixOpNameImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getInfixOpAll();
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        throw new IncorrectOperationException("Can't rename symbolic operator");
    }

    @Override
    public @NotNull TLAplusNameFixness fixness() {
        return TLAplusNameFixness.INFIX;
    }

    @Override
    public String @Nullable [] synonyms() {
        TLAplusInfixOp op = getInfixOpAll().getInfixOp();
        if (op == null) {
            return null;
        }

        if (op.getOpLteq() != null) {
            return LTEQ;
        }
        if (op.getOpCap() != null) {
            return CAP;
        }
        if (op.getOpOplus() != null) {
            return OPLUS;
        }
        if (op.getOpOminus() != null) {
            return OMINUS;
        }
        if (op.getOpOdot() != null) {
            return ODOT;
        }
        if (op.getOpOtimes() != null) {
            return OTIMES;
        }
        if (op.getOpSlash() != null) {
            return OSLASH;
        }
        if (op.getOpGteq() != null) {
            return GTEQ;
        }
        if (op.getOpCup() != null) {
            return CUP;
        }
        if (op.getOpNoteq() != null) {
            return NOTEQ;
        }
        if (op.getOpCirc() != null) {
            return CIRC;
        }
        return null;
    }
}
