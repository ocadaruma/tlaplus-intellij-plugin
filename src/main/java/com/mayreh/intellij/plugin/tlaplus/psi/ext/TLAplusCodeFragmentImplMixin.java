package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusCodeFragment;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public abstract class TLAplusCodeFragmentImplMixin extends TLAplusElementImpl implements TLAplusCodeFragment {
    protected TLAplusCodeFragmentImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(@NotNull TLAplusElement placement) {
        return Stream.empty();
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> publicDefinitions() {
        return Stream.empty();
    }

    @Override
    public @NotNull Stream<TLAplusModule> modulesFromExtends() {
        return Stream.empty();
    }

    @Override
    public @NotNull Stream<TLAplusModule> modulesFromInstantiation(Predicate<TLAplusInstance> requirement) {
        return Stream.empty();
    }

    @Override
    public @NotNull Stream<TLAplusModule> availableModules() {
        return Stream.empty();
    }
}
