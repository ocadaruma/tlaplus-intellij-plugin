package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.util.Optionalx.asInstanceOf;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;

public interface TLAplusElement extends PsiElement {
    /**
     * The module that the element belongs to.
     */
    default @Nullable TLAplusModule currentModule() {
        // For ordinary TLA+ spec, every element should be inside TLAplusModule
        TLAplusModule parentModule = PsiTreeUtil.getParentOfType(this, TLAplusModule.class);
        if (parentModule != null) {
            return parentModule;
        }
        // For TLA+ code fragment, the module that the fragment is executed on is stored in TLAplusFragmentFile's user data
        return asInstanceOf(getContainingFile(), TLAplusFragmentFile.class)
                .flatMap(file -> Optional.ofNullable(file.module()))
                .orElse(null);
    }
}
