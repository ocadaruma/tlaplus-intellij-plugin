package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public interface TLAplusModuleContext extends TLAplusNameContext {
    /**
     * Returns the stream of public definitions.
     */
    @NotNull Stream<TLAplusNamedElement> publicDefinitions();

    /**
     * Find the module from search path (i.e. same directory or standard modules) by module name.
     */
    @Nullable TLAplusModule findModule(String moduleName);

    /**
     * Returns imported modules by extends.
     */
    @NotNull Stream<TLAplusModule> modulesFromExtends();

    /**
     * Returns imported modules by instantiation that meets the requiremtn.
     */
    @NotNull Stream<TLAplusModule> modulesFromInstantiation(Predicate<TLAplusInstance> requirement);
}
