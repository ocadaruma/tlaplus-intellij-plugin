package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;

public interface TLAplusReferenceElement extends TLAplusElement {
    /**
     * Name of this reference.
     * Used for reference resolution.
     */
    @NotNull String getReferenceName();
}
