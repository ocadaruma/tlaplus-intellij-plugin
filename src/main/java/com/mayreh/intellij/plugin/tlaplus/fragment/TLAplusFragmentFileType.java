package com.mayreh.intellij.plugin.tlaplus.fragment;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;

public final class TLAplusFragmentFileType extends LanguageFileType {
    public static final TLAplusFragmentFileType INSTANCE = new TLAplusFragmentFileType();

    private TLAplusFragmentFileType() {
        super(TLAplusFragmentLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "TLA+ code fragment";
    }

    @Override
    public @NotNull @Nls(capitalization = Capitalization.Sentence) String getDescription() {
        return getName();
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
