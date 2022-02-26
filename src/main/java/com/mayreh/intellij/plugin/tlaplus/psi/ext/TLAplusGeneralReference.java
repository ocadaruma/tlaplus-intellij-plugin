package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.Nullable;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstancePrefix;

/**
 * Reference that may be prefixed by instantiated module
 */
public interface TLAplusGeneralReference extends TLAplusElement {

    @Nullable TLAplusInstancePrefix getInstancePrefix();
}
