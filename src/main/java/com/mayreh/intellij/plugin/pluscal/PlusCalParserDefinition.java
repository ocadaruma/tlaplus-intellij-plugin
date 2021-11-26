package com.mayreh.intellij.plugin.pluscal;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.mayreh.intellij.plugin.pluscal.lexer._PlusCalLexer;
import com.mayreh.intellij.plugin.tlaplus.TLAplusParserDefinition;

public class PlusCalParserDefinition extends TLAplusParserDefinition {
    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new FlexAdapter(new _PlusCalLexer(true));
    }
}
