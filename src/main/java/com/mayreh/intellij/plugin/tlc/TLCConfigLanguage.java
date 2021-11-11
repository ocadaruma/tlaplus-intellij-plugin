package com.mayreh.intellij.plugin.tlc;

import com.intellij.lang.Language;

public class TLCConfigLanguage extends Language {
    public static final TLCConfigLanguage INSTANCE = new TLCConfigLanguage();

    private TLCConfigLanguage() {
        super("TLC config");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
