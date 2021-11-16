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
public interface TLAplusNameContext extends PsiElement {
    @Nullable TLAplusNamedElement findDefinition(
            @NotNull TLAplusReferenceElement element);

    default boolean isDefinitionOf(@NotNull TLAplusNamedElement name,
                                   @NotNull TLAplusReferenceElement reference,
                                   boolean checkForwardReference) {
        return Objects.equals(name.getName(), reference.getReferenceName()) &&
               (!checkForwardReference || name.getTextOffset() <= reference.getTextOffset());
    }
}
