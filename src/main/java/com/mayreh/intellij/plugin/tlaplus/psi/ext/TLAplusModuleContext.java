package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.Nullable;

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
    @Nullable TLAplusModuleContext findModule(String moduleName);
}
