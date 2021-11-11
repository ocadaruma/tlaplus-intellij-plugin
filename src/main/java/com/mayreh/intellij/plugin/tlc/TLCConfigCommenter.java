package com.mayreh.intellij.plugin.tlc;

import org.jetbrains.annotations.Nullable;

import com.intellij.lang.Commenter;

public class TLCConfigCommenter implements Commenter {
    @Override
    public @Nullable String getLineCommentPrefix() {
        return "\\*";
    }

    @Override
    public @Nullable String getBlockCommentPrefix() {
        return "(*";
    }

    @Override
    public @Nullable String getBlockCommentSuffix() {
        return "*)";
    }

    @Override
    public @Nullable String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentSuffix() {
        return null;
    }
}
