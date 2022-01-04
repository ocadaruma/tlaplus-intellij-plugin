package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public interface TLAplusModuleContext extends TLAplusNameContext {
    /**
     * Returns the stream of public definitions.
     * searchedModuleNames will be used for infinite recursion guard when a module contains
     * cyclic module reference. (e.g. ModuleA extends ModuleB, ModuleB extends ModuleA)
     */
    @NotNull Stream<TLAplusNamedElement> publicDefinitions(Set<String> searchedModuleNames);

    /**
     * Returns the stream of modules from search path (i.e. same directory or standard modules)
     */
    @NotNull Stream<TLAplusModule> availableModules();

    /**
     * Returns imported modules by extends.
     */
    @NotNull Stream<TLAplusModule> modulesFromExtends();

    /**
     * Returns imported modules by instantiation that meets the requirement.
     */
    @NotNull Stream<TLAplusModule> modulesFromInstantiation(Predicate<TLAplusInstance> requirement);
}
