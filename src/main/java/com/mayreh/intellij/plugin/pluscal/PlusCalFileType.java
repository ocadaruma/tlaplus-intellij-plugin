package com.mayreh.intellij.plugin.pluscal;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;

/**
 * Defining file type is necessary to make syntax-highlighting on injected language works
 */
public final class PlusCalFileType extends LanguageFileType {
    public static final PlusCalFileType INSTANCE = new PlusCalFileType();

    private PlusCalFileType() {
        super(PlusCalLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "PlusCal";
    }

    @Override
    public @NotNull @Nls(capitalization = Capitalization.Sentence) String getDescription() {
        return "PlusCal formal specification language";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Any_type;
    }
}
