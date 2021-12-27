package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.util.Optionalx.asInstanceOf;

import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusCodeFragment;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public abstract class TLAplusCodeFragmentImplMixin extends TLAplusElementImpl implements TLAplusCodeFragment {
    protected TLAplusCodeFragmentImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(@NotNull TLAplusElement placement) {
        return asInstanceOf(getContainingFile(), TLAplusFragmentFile.class)
                .flatMap(file -> Optional.ofNullable(file.module())).stream()
                .flatMap(module -> module.localDefinitions(placement));
    }
}
