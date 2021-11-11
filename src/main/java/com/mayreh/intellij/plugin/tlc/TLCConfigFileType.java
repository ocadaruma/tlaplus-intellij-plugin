package com.mayreh.intellij.plugin.tlc;

import javax.swing.Icon;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;

public class TLCConfigFileType extends LanguageFileType {
    public static final TLCConfigFileType INSTANCE = new TLCConfigFileType();

    private TLCConfigFileType() {
        super(TLCConfigLanguage.INSTANCE);
    }

    @Override
    public String getName() {
        return "TLC config";
    }

    @Override
    public String getDescription() {
        return "TLC model checker configuration";
    }

    @Override
    public String getDefaultExtension() {
        return "cfg";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Any_type;
    }
}
