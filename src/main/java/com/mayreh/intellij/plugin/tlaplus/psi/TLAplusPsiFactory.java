package com.mayreh.intellij.plugin.tlaplus.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFileType;

public class TLAplusPsiFactory {
    private final Project project;

    public TLAplusPsiFactory(Project project) {
        this.project = project;
    }

    public TLAplusFile createFile(String text) {
        return (TLAplusFile) PsiFileFactory.getInstance(project)
                                           .createFileFromText("DUMMY.tla", TLAplusFileType.INSTANCE, text);
    }

    public PsiElement createIdentifier(String text) {
        return PsiTreeUtil.findChildOfType(createFile("---- MODULE " + text + " ----"),
                                           TLAplusModuleHeader.class).getNameIdentifier();
    }
}
