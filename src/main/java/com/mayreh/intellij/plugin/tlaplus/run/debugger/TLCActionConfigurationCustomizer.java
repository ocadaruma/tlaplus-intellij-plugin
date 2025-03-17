package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.impl.DynamicActionConfigurationCustomizer;
import com.intellij.xdebugger.impl.actions.XDebuggerActions;
import com.mayreh.intellij.plugin.tlaplus.run.debugger.TLCActionOverrides.TLCForceRunToCursorAction;
import com.mayreh.intellij.plugin.tlaplus.run.debugger.TLCActionOverrides.TLCRunToCursorAction;

public class TLCActionConfigurationCustomizer implements DynamicActionConfigurationCustomizer {
    @Override
    public void registerActions(@NotNull ActionManager actionManager) {
        // These actions aren't supported for now.
        // Technically it's possible by setting dummy breakpoint on TLCDebugger at the caret position though.
        maybeReplace(actionManager, XDebuggerActions.RUN_TO_CURSOR, TLCRunToCursorAction::new);
        maybeReplace(actionManager, XDebuggerActions.FORCE_RUN_TO_CURSOR, TLCForceRunToCursorAction::new);
    }

    @Override
    public void unregisterActions(@NotNull ActionManager actionManager) {
        // noop since replaced action acts identical to original action for non-TLA+ files
    }

    private static void maybeReplace(ActionManager actionManager, String actionId, Supplier<AnAction> replacement) {
        AnAction original = actionManager.getAction(actionId);
        if (original == null) {
            return;
        }
        AnAction newAction = replacement.get();
        // Necessary to copy presentations from original.
        // Otherwise, icons and popover texts might not work even on non-TLA+ files because action presentations
        // are set externally (e.g. from xml, bundles), which isn't inherited just by extending original action
        newAction.copyFrom(original);
        actionManager.replaceAction(actionId, newAction);
    }
}
