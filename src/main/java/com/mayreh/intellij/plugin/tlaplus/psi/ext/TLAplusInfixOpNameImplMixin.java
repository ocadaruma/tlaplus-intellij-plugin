package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
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
        return Stream.of(getInfixOp11(),
                         getInfixOp22(),
                         getInfixOp33(),
                         getInfixOp55(),
                         getInfixOp514(),
                         getInfixOp66(),
                         getInfixOp77(),
                         getInfixOp88(),
                         getInfixOp99(),
                         getInfixOp913(),
                         getInfixOp914(),
                         getInfixOp1010(),
                         getInfixOp1011(),
                         getInfixOp1111(),
                         getInfixOp1313(),
                         getInfixOp1414()
                     ).filter(Objects::nonNull)
                     .findFirst()
                     .orElse(null);
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
        if (getInfixOp55() != null) {
            if (getInfixOp55().getOpLteq() != null) {
                return LTEQ;
            }
            if (getInfixOp55().getOpGteq() != null) {
                return GTEQ;
            }
            if (getInfixOp55().getOpNoteq() != null) {
                return NOTEQ;
            }
        }
        if (getInfixOp88() != null) {
            if (getInfixOp88().getOpCap() != null) {
                return CAP;
            }
            if (getInfixOp88().getOpCup() != null) {
                return CUP;
            }
        }
        if (getInfixOp1010() != null) {
            if (getInfixOp1010().getOpOplus() != null) {
                return OPLUS;
            }
        }
        if (getInfixOp1111() != null) {
            if (getInfixOp1111().getOpOminus() != null) {
                return OMINUS;
            }
        }
        if (getInfixOp1313() != null) {
            if (getInfixOp1313().getOpOdot() != null) {
                return ODOT;
            }
            if (getInfixOp1313().getOpOtimes() != null) {
                return OTIMES;
            }
            if (getInfixOp1313().getOpOslash() != null) {
                return OSLASH;
            }
            if (getInfixOp1313().getOpCirc() != null) {
                return CIRC;
            }
        }
        return null;
    }
}
