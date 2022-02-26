package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.Nullable;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstancePrefix;

public interface TLAplusGeneralReference extends TLAplusElement {
    @Nullable TLAplusInstancePrefix getInstancePrefix();
}
