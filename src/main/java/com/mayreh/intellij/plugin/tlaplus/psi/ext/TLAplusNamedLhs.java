package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpName;

/**
 * Interface for LHS of the op definition (`==`).
 */
public interface TLAplusNamedLhs extends TLAplusElement {
    /**
     * The element that defines operator name
     */
    @NotNull TLAplusNamedElement getNamedElement();

    /**
     * Argument names of this operator
     */
    @NotNull Stream<TLAplusOpName> opNames();
}
