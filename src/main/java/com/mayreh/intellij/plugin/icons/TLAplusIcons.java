package com.mayreh.intellij.plugin.icons;

import javax.swing.Icon;

import com.intellij.openapi.util.IconLoader;

public class TLAplusIcons {
    public static final Icon TLA_PLUS = load("/icons/tlaplus.svg");

    private static Icon load(String path) {
        return IconLoader.getIcon(path, TLAplusIcons.class);
    }
}
