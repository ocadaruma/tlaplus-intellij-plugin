package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import org.jetbrains.annotations.NotNull;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.mayreh.intellij.plugin.tlaplus.TLAplusParserDefinition;
import com.mayreh.intellij.plugin.tlaplus.lexer._TLCErrorTraceLexer;

public class TLCErrorTraceParserDefinition extends TLAplusParserDefinition {
    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new FlexAdapter(new _TLCErrorTraceLexer(null));
    }
}
