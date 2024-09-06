
package com.mayreh.intellij.plugin.tlaplus.ide.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFileType;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentLanguage;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.run.eval.DummyModule;
import com.mayreh.intellij.plugin.tlaplus.run.eval.EvaluateExpressionDialog;
import com.mayreh.intellij.plugin.util.TLAplusTreeUtil;

public class EvaluateExpressionAction extends DumbAwareAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(e.getProject() != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null) {
            return;
        }
        new EvaluateExpressionDialog(e.getProject(), null, new XDebuggerEditorsProviderBase() {
            @Override
            protected PsiFile createExpressionCodeFragment(
                    @NotNull Project project,
                    @NotNull String text,
                    @Nullable PsiElement elementContext,
                    boolean isPhysical) {

                String dummyFileName = DummyModule.moduleName() + ".tla";
                TLAplusFile dummyModuleFile = (TLAplusFile) PsiFileFactory
                        .getInstance(project)
                        .createFileFromText(dummyFileName,
                                            TLAplusLanguage.INSTANCE,
                                            DummyModule.builder().forCompletion().buildAsString());

                TLAplusFragmentFile fragment = (TLAplusFragmentFile) PsiFileFactory
                        .getInstance(project)
                        .createFileFromText(dummyFileName,
                                            TLAplusFragmentLanguage.INSTANCE,
                                            text);

                TLAplusTreeUtil.findChildOfType(dummyModuleFile, TLAplusModule.class).ifPresent(fragment::setModule);
                return fragment;
            }

            @Override
            public @NotNull FileType getFileType() {
                return TLAplusFragmentFileType.INSTANCE;
            }
        }, XExpressionImpl.EMPTY_EXPRESSION).show();
    }
}
