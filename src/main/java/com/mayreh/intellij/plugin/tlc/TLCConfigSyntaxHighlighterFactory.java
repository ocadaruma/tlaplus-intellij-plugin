package com.mayreh.intellij.plugin.tlc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class TLCConfigSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    @Override
    public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project,
                                                           @Nullable VirtualFile virtualFile) {
        return new TLCConfigSyntaxHighlighter();
    }
}
