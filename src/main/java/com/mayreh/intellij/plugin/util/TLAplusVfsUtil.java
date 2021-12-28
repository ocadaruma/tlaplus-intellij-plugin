package com.mayreh.intellij.plugin.util;

import java.net.URL;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

public class TLAplusVfsUtil {
    public static Optional<PsiFile> findFile(@NotNull Project project, @Nullable URL url) {
        if (url == null) {
            return Optional.empty();
        }
        VirtualFile file = VfsUtil.findFileByURL(url);
        if (file == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(PsiManager.getInstance(project).findFile(file));
    }

//    public static Optional<PsiFile> findFile(@NotNull Project project, @Nullable URL url) {
//
//    }
}
