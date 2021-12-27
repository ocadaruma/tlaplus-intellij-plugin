package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile.CodeFragmentContext;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusCodeFragment;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public abstract class TLAplusCodeFragmentImplMixin extends TLAplusElementImpl implements TLAplusCodeFragment {
    protected TLAplusCodeFragmentImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(@NotNull TLAplusElement placement) {
        return Optional.ofNullable(currentModule())
                       .map(module -> module.localDefinitions(placement))
                       .orElse(Stream.empty());
    }

    @Override
    public @Nullable TLAplusModule currentModule() {
        if (!(getContainingFile() instanceof TLAplusFragmentFile)) {
            return null;
        }

        return Optional.ofNullable(((TLAplusFragmentFile) getContainingFile()).codeFragmentContext())
                       .map(CodeFragmentContext::dummyModule)
                       .orElse(null);
    }
}
