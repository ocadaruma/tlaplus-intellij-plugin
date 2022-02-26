package com.mayreh.intellij.plugin.tlc.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.util.PsiTreeUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.ext.TLAplusReferenceElement;

public abstract class TLCConfigIdentifierImplMixin
        extends ASTWrapperPsiElement implements ContributedReferenceHost, TLAplusReferenceElement {
    protected TLCConfigIdentifierImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    public abstract PsiElement getIdentifier();

    @Override
    public @NotNull String getReferenceName() {
        return getIdentifier().getText();
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return PsiReferenceService.getService().getContributedReferences(this);
    }

    @Override
    public PsiElement getContext() {
        return currentModule();
    }

    @Override
    public @Nullable TLAplusModule currentModule() {
        if (getContainingFile() == null) {
            return null;
        }
        // We have to use getOriginalFile here because getContainingDirectory and getVirtualFile may return null
        // when this element is a temporary psi (e.g. when getting variants for completion)
        PsiFile psiFile = getContainingFile().getOriginalFile();

        PsiDirectory directory = psiFile.getContainingDirectory();
        if (directory == null) {
            return null;
        }

        VirtualFile vFile = psiFile.getVirtualFile();
        if (vFile == null) {
            return null;
        }

        String moduleFileName = vFile.getNameWithoutExtension() + ".tla";

        PsiFile moduleFile = directory.findFile(moduleFileName);
        if (moduleFile == null) {
            return null;
        }

        return PsiTreeUtil.findChildOfType(moduleFile, TLAplusModule.class);
    }
}
