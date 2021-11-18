package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.Nullable;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public interface TLAplusModuleContext extends TLAplusNameContext {
    /**
     * Find the definition from this module with taking
     * visibilities into account. (e.g. "LOCAL" definitions will not be returned)
     */
    @Nullable TLAplusNamedElement findPublicDefinition(String referenceName);

    /**
     * Find the module from search path (i.e. same directory) by module name.
     */
    @Nullable TLAplusModule findModule(String moduleName);

    /**
     * Resolve module name to {@link TLAplusModule} which is exported publicly from this context.
     */
    @Nullable TLAplusModule resolveModulePublic(String moduleName);
}
