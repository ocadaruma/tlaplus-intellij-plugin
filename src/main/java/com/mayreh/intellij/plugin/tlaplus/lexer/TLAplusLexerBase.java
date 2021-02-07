package com.mayreh.intellij.plugin.tlaplus.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.lexer.FlexLexer;

public abstract class TLAplusLexerBase implements FlexLexer {
    private static final Pattern MODULE_BEGIN;
    static {
        MODULE_BEGIN = Pattern.compile("-----*([ \t\f]|\\R(\\R|[ \t\f])*)*MODULE");
    }

    public abstract CharSequence yytext();
    public abstract void yypushback(int num);

    public void pushbackModuleBegin() {
        Matcher m = MODULE_BEGIN.matcher(yytext());
        if (m.find()) {
            yypushback(m.end() - m.start());
        }
    }
}
