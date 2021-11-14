package com.mayreh.intellij.plugin.tlc;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;

public class TLCConfigFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {
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
        return "";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Any_type;
    }

    /**
     * Since .cfg is a common extension widely used by other tools (e.g. Ansible),
     * we resolve file type to TLC model checker config only when .tla file exists in
     * same directory.
     */
    @Override
    public boolean isMyFileType(@NotNull VirtualFile file) {
        if (!"cfg".equals(file.getExtension())) {
            return false;
        }
        if (file.getParent() == null) {
            return false;
        }
        return file.getParent().findChild(file.getNameWithoutExtension() + ".tla") != null;
    }
}
