
package com.mayreh.intellij.plugin.tlaplus.ide.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.intellij.xdebugger.impl.evaluate.XDebuggerEvaluationDialog;
import com.mayreh.intellij.plugin.fragment.TLAplusCodeFragmentFileType;
import com.mayreh.intellij.plugin.fragment.TLAplusCodeFragmentLanguage;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFileType;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;
import com.mayreh.intellij.plugin.tlaplus.run.eval.EvaluateExpressionDialog;
import com.mayreh.intellij.plugin.tlaplus.run.eval.ExpressionEvaluator;
import com.mayreh.intellij.plugin.tlaplus.run.eval.Result;
import com.mayreh.intellij.plugin.util.StringUtil;

import pcal.PlusCalTranslator;

public class EvaluateExpressionAction extends DumbAwareAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Document document = currentDocument(e);
        e.getPresentation().setEnabled(document != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Document document = currentDocument(e);
        Project project = e.getProject();
        if (project == null || document == null) {
            return;
        }

        FileDocumentManager.getInstance().saveAllDocuments();
//        XDebuggerEvaluator evaluator = new XDebuggerEvaluator() {
//            @Override
//            public void evaluate(@NotNull String expression,
//                                 @NotNull XEvaluationCallback callback,
//                                 @Nullable XSourcePosition expressionPosition) {
//                Result result = ExpressionEvaluator.evaluate(null, expression);
//                if (!result.errors().isEmpty()) {
//                    callback.errorOccurred(String.join("\n", result.errors()));
//                } else {
//                    callback.evaluated(new XValue() {
//                        @Override
//                        public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
//                            node.setPresentation(null, null, result.output(), false);
//                        }
//                    });
//                }
//            }
//        };
        new EvaluateExpressionDialog(project, new XDebuggerEditorsProviderBase() {
            @Override
            protected PsiFile createExpressionCodeFragment(
                    @NotNull Project project,
                    @NotNull String text,
                    @Nullable PsiElement context,
                    boolean isPhysical) {
                return PsiFileFactory.getInstance(project)
                                     .createFileFromText("__DUMMY__.tla", TLAplusCodeFragmentLanguage.INSTANCE, text, true, true);
            }

            @Override
            public @NotNull FileType getFileType() {
                return TLAplusCodeFragmentFileType.INSTANCE;
            }
        }, XExpressionImpl.EMPTY_EXPRESSION).show();
    }

    private static @Nullable Document currentDocument(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return null;
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) {
            editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        }
        if (editor == null) {
            return null;
        }
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null || !file.isInLocalFileSystem() || TLAplusFileType.INSTANCE != file.getFileType()) {
            return null;
        }

        return document;
    }
}
