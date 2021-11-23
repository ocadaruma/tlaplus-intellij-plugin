package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

/**
 * Interface for elements that can be considered as "context" (scope).
 * e.g. Module, LET expression,...
 */
public interface TLAplusNameContext extends TLAplusElement {
    /**
     * Returns the stream of definition in this context.
     * This method is intended and should be implemented to find the definition from
     * same module locally.
     *
     * To lookup definitions from other modules by reference name,
     * you should use {@link TLAplusModuleContext#publicDefinitions()}.
     */
    @NotNull Stream<TLAplusNamedElement> localDefinitions(@NotNull TLAplusReferenceElement reference);
}
