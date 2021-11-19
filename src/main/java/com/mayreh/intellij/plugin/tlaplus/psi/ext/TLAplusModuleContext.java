package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusInstance;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public interface TLAplusModuleContext extends TLAplusNameContext {
    /**
     * Find the definition from this module with taking
     * visibilities into account. (e.g. "LOCAL" definitions will not be returned)
     */
    @Nullable TLAplusNamedElement findPublicDefinition(String referenceName);

    /**
     * Find the module from search path (i.e. same directory or standard modules) by module name.
     */
    @Nullable TLAplusModule findModule(String moduleName);

    /**
     * Resolve module name to {@link TLAplusModule} which is exported publicly from this context.
     */
    @Nullable TLAplusModule resolveModulePublic(String moduleName);

    /**
     * Execute finder in imported modules by extends and return the result if found (i.e. finder returns non-null)
     */
    @Nullable <T> T findFromExtends(Function<TLAplusModule, @Nullable T> finder);

    /**
     * Execute finder in imported modules by instantiation and return the result if found (i.e. finder returns non-null).
     * For each instantiation, it's checked if the instantiation meets the requirement.
     */
    @Nullable <T> T findFromInstantiation(
            Predicate<TLAplusInstance> requirement,
            Function<TLAplusModule, @Nullable T> finder);
}
