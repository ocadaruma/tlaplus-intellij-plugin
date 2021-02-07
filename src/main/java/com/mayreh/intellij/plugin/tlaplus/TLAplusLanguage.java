package com.mayreh.intellij.plugin.tlaplus;

import com.intellij.lang.Language;

public class TLAplusLanguage extends Language {
    public static final TLAplusLanguage INSTANCE = new TLAplusLanguage();

    private TLAplusLanguage() {
        super("TLA+");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
