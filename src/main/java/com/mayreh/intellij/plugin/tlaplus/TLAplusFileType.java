package com.mayreh.intellij.plugin.tlaplus;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;

public final class TLAplusFileType extends LanguageFileType {
    public static final TLAplusFileType INSTANCE = new TLAplusFileType();

    private TLAplusFileType() {
        super(TLAplusLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "TLA+";
    }

    @Override
    public @NotNull @Nls(capitalization = Capitalization.Sentence) String getDescription() {
        return "TLA+ formal specification language";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "tla";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Any_type;
    }
}
