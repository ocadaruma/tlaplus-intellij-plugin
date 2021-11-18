package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

/**
 * Interface for elements that can be considered as "context" (scope).
 * e.g. Module, LET expression,...
 */
public interface TLAplusNameContext extends TLAplusElement {
    /**
     * Find the definition of the reference from this context.
     * This method is intended and should be implemented to find the definition from
     * same module locally.
     *
     * To lookup definitions from other modules by reference name,
     * you should use {@link TLAplusModuleContext#findPublicDefinition(String)}.
     */
    @Nullable TLAplusNamedElement findLocalDefinition(
            @NotNull TLAplusReferenceElement element);

    /**
     * Check if a candidate {@link TLAplusNamedElement} name can be considered as
     * the definition of the reference.
     */
    default boolean isLocalDefinitionOf(@NotNull TLAplusNamedElement name,
                                        @NotNull TLAplusReferenceElement reference,
                                        boolean checkForwardReference) {
        if (!Objects.equals(name.getContainingFile(), reference.getContainingFile())) {
            return false;
        }
        return Objects.equals(name.getName(), reference.getReferenceName()) &&
               (!checkForwardReference || name.getTextOffset() <= reference.getTextOffset());
    }
}
