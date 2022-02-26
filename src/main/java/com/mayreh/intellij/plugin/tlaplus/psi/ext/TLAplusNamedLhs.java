package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusOpName;

public interface TLAplusNamedLhs extends TLAplusElement {
    @NotNull TLAplusNamedElement getNamedElement();

    @NotNull Stream<TLAplusOpName> opNames();
}
