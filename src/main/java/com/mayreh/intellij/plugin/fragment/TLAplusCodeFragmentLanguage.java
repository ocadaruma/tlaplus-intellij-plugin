package com.mayreh.intellij.plugin.fragment;

import com.intellij.lang.Language;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;

public class TLAplusCodeFragmentLanguage extends Language {
    public static final TLAplusCodeFragmentLanguage INSTANCE = new TLAplusCodeFragmentLanguage();

    private TLAplusCodeFragmentLanguage() {
        super(TLAplusLanguage.INSTANCE,"TLA+ code fragment");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
