package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile.CodeFragmentContext;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusCodeFragment;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.run.eval.DummyModule;

public abstract class TLAplusCodeFragmentImplMixin extends TLAplusElementImpl implements TLAplusCodeFragment {
    protected TLAplusCodeFragmentImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(@NotNull TLAplusElement placement) {
        return delegate(module -> module.localDefinitions(placement));
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> publicDefinitions() {
        return delegate(TLAplusModuleContext::publicDefinitions);
    }

    @Override
    public @NotNull Stream<TLAplusModule> modulesFromExtends() {
        return delegate(TLAplusModuleContext::modulesFromExtends);
    }

    @Override
    public @NotNull Stream<TLAplusModule> modulesFromInstantiation(Predicate<TLAplusInstance> requirement) {
        return delegate(module -> module.modulesFromInstantiation(requirement));
    }

    @Override
    public @NotNull Stream<TLAplusModule> availableModules() {
        Stream<TLAplusModule> modules = delegate(TLAplusModuleContext::availableModules);

        final Stream<TLAplusModule> modulesInSameDir;
        if (getContainingFile() instanceof TLAplusFragmentFile) {
            modulesInSameDir = Optional
                    .ofNullable(((TLAplusFragmentFile) getContainingFile()).codeFragmentContext()).stream()
                    .flatMap(ctx -> Optional
                            .ofNullable(VfsUtil.findFile(ctx.directory(), true)).stream()
                            .flatMap(dir -> Optional
                                    .ofNullable(PsiManager.getInstance(getProject()).findDirectory(dir)).stream()
                                    .flatMap(psiDir -> Arrays
                                            .stream(psiDir.getFiles())
                                            .filter(f -> f.getName().endsWith(".tla") &&
                                                         !f.getName().equals(DummyModule.moduleName() + ".tla"))
                                            .flatMap(f -> Optional.ofNullable(PsiTreeUtil.findChildOfType(f, TLAplusModule.class)).stream()))
                            )
                    );
        } else {
            modulesInSameDir = Stream.empty();
        }
        return Stream.concat(modules, modulesInSameDir);
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

    private @NotNull <T> Stream<T> delegate(Function<TLAplusModule, Stream<T>> f) {
        TLAplusModule module = currentModule();
        if (module == null) {
            return Stream.empty();
        }

        return f.apply(module);
    }
}
