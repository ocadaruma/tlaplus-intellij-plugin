package com.mayreh.intellij.plugin.tlaplus.run.debugger;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.impl.actions.ForceRunToCursorAction;
import com.intellij.xdebugger.impl.actions.RunToCursorAction;
import com.mayreh.intellij.plugin.tlaplus.run.TLCRunConfiguration;

public class TLCActionOverrides {
    public static class TLCRunToCursorAction extends RunToCursorAction {
        @Override
        protected boolean isEnabled(AnActionEvent e) {
            if (isTLAplusConfig(e.getDataContext())) {
                return false;
            }
            return super.isEnabled(e);
        }

        @Override
        protected boolean isHidden(AnActionEvent event) {
            if (isTLAplusConfig(event.getDataContext())) {
                return true;
            }
            return super.isHidden(event);
        }
    }

    public static class TLCForceRunToCursorAction extends ForceRunToCursorAction {
        @Override
        protected boolean isEnabled(AnActionEvent e) {
            if (isTLAplusConfig(e.getDataContext())) {
                return false;
            }
            return super.isEnabled(e);
        }

        @Override
        protected boolean isHidden(AnActionEvent event) {
            if (isTLAplusConfig(event.getDataContext())) {
                return true;
            }
            return super.isHidden(event);
        }
    }

    private static boolean isTLAplusConfig(DataContext context) {
        Project project = CommonDataKeys.PROJECT.getData(context);
        if (project == null) {
            return false;
        }
        RunnerAndConfigurationSettings settings = RunManager.getInstance(project)
                                                            .getSelectedConfiguration();
        return settings != null && settings.getConfiguration() instanceof TLCRunConfiguration;
    }
}
