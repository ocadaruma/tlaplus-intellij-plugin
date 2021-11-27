package com.mayreh.intellij.plugin.pluscal;

import org.intellij.lang.regexp.RegExpLanguage;
import org.intellij.lang.regexp.ecmascript.EcmaScriptRegexpLanguage;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;

/**
 * Defining PlusCal language for injecting into TLA+'s comment section.
 * refs: relation between {@link EcmaScriptRegexpLanguage} and {@link RegExpLanguage}
 */
public class PlusCalLanguage extends Language implements InjectableLanguage {
    public static final PlusCalLanguage INSTANCE = new PlusCalLanguage();

    private PlusCalLanguage() {
        super(TLAplusLanguage.INSTANCE,"PlusCal");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
