package com.mayreh.intellij.plugin.tlaplus.psi;

import javax.swing.Icon;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

import lombok.RequiredArgsConstructor;

/**
 * {@link TLAplusNamedElement} that doesn't exist in actual PSI tree but just
 * used to tell {@link FindUsagesHandler}'s caller to search usages of synonyms not only for original op-name.
 */
@RequiredArgsConstructor
public class TLAplusSynonymElement implements TLAplusNamedElement {
    private final TLAplusNamedElement delegate;
    private final String name;

    @Override
    public @NotNull TLAplusNameFixness fixness() {
        return delegate.fixness();
    }

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return delegate.getPresentation();
    }

    @Override
    public void navigate(boolean requestFocus) {
        delegate.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return delegate.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return delegate.canNavigateToSource();
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return delegate.getNameIdentifier();
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        return delegate.setName(name);
    }

    @Override
    public @NotNull Project getProject() {
        return delegate.getProject();
    }

    @Override
    public @NotNull Language getLanguage() {
        return delegate.getLanguage();
    }

    @Override
    public PsiManager getManager() {
        return delegate.getManager();
    }

    @Override
    public PsiElement @NotNull [] getChildren() {
        return delegate.getChildren();
    }

    @Override
    public PsiElement getParent() {
        return delegate.getParent();
    }

    @Override
    public PsiElement getFirstChild() {
        return delegate.getFirstChild();
    }

    @Override
    public PsiElement getLastChild() {
        return delegate.getLastChild();
    }

    @Override
    public PsiElement getNextSibling() {
        return delegate.getNextSibling();
    }

    @Override
    public PsiElement getPrevSibling() {
        return delegate.getPrevSibling();
    }

    @Override
    public PsiFile getContainingFile() {
        return delegate.getContainingFile();
    }

    @Override
    public TextRange getTextRange() {
        return delegate.getTextRange();
    }

    @Override
    public int getStartOffsetInParent() {
        return delegate.getStartOffsetInParent();
    }

    @Override
    public int getTextLength() {
        return delegate.getTextLength();
    }

    @Override
    public @Nullable PsiElement findElementAt(int offset) {
        return delegate.findElementAt(offset);
    }

    @Override
    public @Nullable PsiReference findReferenceAt(int offset) {
        return delegate.findReferenceAt(offset);
    }

    @Override
    public int getTextOffset() {
        return delegate.getTextOffset();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public char @NotNull [] textToCharArray() {
        return delegate.textToCharArray();
    }

    @Override
    public PsiElement getNavigationElement() {
        return delegate.getNavigationElement();
    }

    @Override
    public PsiElement getOriginalElement() {
        return delegate.getOriginalElement();
    }

    @Override
    public boolean textMatches(@NotNull @NonNls CharSequence text) {
        return delegate.textMatches(text);
    }

    @Override
    public boolean textMatches(@NotNull PsiElement element) {
        return delegate.textMatches(element);
    }

    @Override
    public boolean textContains(char c) {
        return delegate.textContains(c);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        delegate.accept(visitor);
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
        delegate.acceptChildren(visitor);
    }

    @Override
    public PsiElement copy() {
        return delegate.copy();
    }

    @Override
    public PsiElement add(@NotNull PsiElement element) {
        return delegate.add(element);
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, @Nullable PsiElement anchor) {
        return delegate.addBefore(element, anchor);
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement element, @Nullable PsiElement anchor) {
        return delegate.addAfter(element, anchor);
    }

    @Override
    public void checkAdd(@NotNull PsiElement element) {
        delegate.checkAdd(element);
    }

    @Override
    public PsiElement addRange(PsiElement first, PsiElement last) {
        return delegate.addRange(first, last);
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) {
        return delegate.addRangeBefore(first, last, anchor);
    }

    @Override
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) {
        return delegate.addRangeAfter(first, last, anchor);
    }

    @Override
    public void delete() {
        delegate.delete();
    }

    @Override
    public void checkDelete() {
        delegate.checkDelete();
    }

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last) {
        delegate.deleteChildRange(first, last);
    }

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) {
        return delegate.replace(newElement);
    }

    @Override
    public boolean isValid() {
        return delegate.isValid();
    }

    @Override
    public boolean isWritable() {
        return delegate.isWritable();
    }

    @Override
    public @Nullable PsiReference getReference() {
        return delegate.getReference();
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return delegate.getReferences();
    }

    @Override
    public <T> @Nullable T getCopyableUserData(@NotNull Key<T> key) {
        return delegate.getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(@NotNull Key<T> key, @Nullable T value) {
        delegate.putCopyableUserData(key, value);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return delegate.processDeclarations(processor, state, lastParent, place);
    }

    @Override
    public @Nullable PsiElement getContext() {
        return delegate.getContext();
    }

    @Override
    public boolean isPhysical() {
        return delegate.isPhysical();
    }

    @Override
    public @NotNull GlobalSearchScope getResolveScope() {
        return delegate.getResolveScope();
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return delegate.getUseScope();
    }

    @Override
    public ASTNode getNode() {
        return delegate.getNode();
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return delegate.isEquivalentTo(another);
    }

    @Override
    public Icon getIcon(int flags) {
        return delegate.getIcon(flags);
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return delegate.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        delegate.putUserData(key, value);
    }
}
