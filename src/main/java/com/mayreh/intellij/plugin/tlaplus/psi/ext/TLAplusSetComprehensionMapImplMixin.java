package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusBoundName;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusSetComprehensionMap;

public abstract class TLAplusSetComprehensionMapImplMixin
        extends TLAplusElementImpl implements TLAplusSetComprehensionMap {
    protected TLAplusSetComprehensionMapImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable TLAplusNamedElement findDefinition(TLAplusReferenceElement element) {
        for (TLAplusBoundName boundName : getBoundNameList()) {
            if (isDefinitionOf(boundName, element, false)) {
                return boundName;
            }
        }
        return null;
    }
}
