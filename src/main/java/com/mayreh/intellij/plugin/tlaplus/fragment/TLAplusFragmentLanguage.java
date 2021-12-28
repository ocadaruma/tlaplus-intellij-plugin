package com.mayreh.intellij.plugin.tlaplus.fragment;

import com.intellij.lang.Language;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;

public class TLAplusFragmentLanguage extends Language {
    public static final TLAplusFragmentLanguage INSTANCE = new TLAplusFragmentLanguage();

    private TLAplusFragmentLanguage() {
        super(TLAplusLanguage.INSTANCE,"TLA+ code fragment");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
