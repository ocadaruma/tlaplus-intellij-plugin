package com.mayreh.intellij.plugin.pluscal;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;

public class PlusCalLanguage extends Language implements InjectableLanguage {
    public static final PlusCalLanguage INSTANCE = new PlusCalLanguage();

    private PlusCalLanguage() {
        super("PlusCal");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
