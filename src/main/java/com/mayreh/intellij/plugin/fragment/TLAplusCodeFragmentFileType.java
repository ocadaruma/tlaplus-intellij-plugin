package com.mayreh.intellij.plugin.fragment;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.mayreh.intellij.plugin.pluscal.PlusCalLanguage;

public final class TLAplusCodeFragmentFileType extends LanguageFileType {
    public static final TLAplusCodeFragmentFileType INSTANCE = new TLAplusCodeFragmentFileType();

    private TLAplusCodeFragmentFileType() {
        super(TLAplusCodeFragmentLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "TLA+ code fragment";
    }

    @Override
    public @NotNull @Nls(capitalization = Capitalization.Sentence) String getDescription() {
        return "TLA";
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
