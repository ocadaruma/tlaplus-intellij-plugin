package com.mayreh.intellij.plugin.tlaplus.psi;

/**
 * Represents fix-ness of this name.
 * Even two name have same names, they will be considered as different reference
 * unless fix-ness matches. (e.g. `-` can be used as both prefix-op and infix-op)
 */
public enum TLAplusNameFixness {
    NONFIX,
    PREFIX,
    INFIX,
    POSTFIX,
}
