package com.mayreh.intellij.plugin.tlaplus.run;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons.RunConfigurations.TestState;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;

public class TLCRunLineMarkerContributor extends RunLineMarkerContributor {
    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (PsiUtilCore.getElementType(element) != TLAplusElementTypes.IDENTIFIER ||
            PsiUtilCore.getElementType(element.getParent()) != TLAplusElementTypes.MODULE_HEADER) {
            return null;
        }
        AnAction[] actions = ExecutorAction.getActions(0);
        return new Info(TestState.Run, actions, e ->
                StringUtil.join(ContainerUtil.mapNotNull(actions, a -> getText(a, e)), "\n"));
    }
}
