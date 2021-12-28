
package com.mayreh.intellij.plugin.tlaplus.ide.actions;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFile;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentFileType;
import com.mayreh.intellij.plugin.tlaplus.fragment.TLAplusFragmentLanguage;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.run.eval.Context;
import com.mayreh.intellij.plugin.tlaplus.run.eval.DummyModule;
import com.mayreh.intellij.plugin.tlaplus.run.eval.EvaluateExpressionDialog;
import com.mayreh.intellij.plugin.util.TLAplusTreeUtil;

public class EvaluateExpressionOnModuleAction extends TLAplusActionBase {
    @Override
    protected void doAction(@NotNull AnActionEvent e,
                            @NotNull Project project,
                            @NotNull TLAplusDocument document) {
        TLAplusModule currentModule = PsiTreeUtil.findChildOfType(document.file(), TLAplusModule.class);
        final String currentModuleName;
        if (currentModule != null) {
            currentModuleName = currentModule.getModuleHeader().getName();
        } else {
            currentModuleName = null;
        }

        Pair<Context, PsiDirectory> context = maybeContext(currentModuleName, document);
        FileDocumentManager.getInstance().saveAllDocuments();

        new EvaluateExpressionDialog(project, context == null ? null : context.first, new XDebuggerEditorsProviderBase() {
            @Override
            protected PsiFile createExpressionCodeFragment(
                    @NotNull Project project,
                    @NotNull String text,
                    @Nullable PsiElement elementContext,
                    boolean isPhysical) {

                DummyModule.Builder moduleBuilder = DummyModule.builder().forCompletion();
                if (currentModuleName != null) {
                    moduleBuilder.extend(currentModuleName);
                }

                String dummyFileName = DummyModule.moduleName() + ".tla";
                TLAplusFile dummyModuleFile = (TLAplusFile) PsiFileFactory
                        .getInstance(project)
                        .createFileFromText(dummyFileName,
                                            TLAplusLanguage.INSTANCE,
                                            moduleBuilder.buildAsString());

                TLAplusFragmentFile fragment = (TLAplusFragmentFile) PsiFileFactory
                        .getInstance(project)
                        .createFileFromText(dummyFileName,
                                            TLAplusFragmentLanguage.INSTANCE,
                                            text);

                // We can't set currentModule as the fragment's module directory because:
                // - currentModule's local definitions (e.g. LOCAL operators) shouldn't be appeared in
                //   completion candidates because they can't be referred when evaluating an expression (i.e.
                //   it creates dummy module that extends currentModule, so only can refer public definitions).
                //   We set dummy module here to resolve references in same logic as expression evaluation.
                TLAplusTreeUtil.findChildOfType(dummyModuleFile, TLAplusModule.class).ifPresent(fragment::setModule);
                if (context != null) {
                    dummyModuleFile.setDirectory(context.second);
                }
                return fragment;
            }

            @Override
            public @NotNull FileType getFileType() {
                return TLAplusFragmentFileType.INSTANCE;
            }
        }, XExpressionImpl.EMPTY_EXPRESSION).show();
    }

    private static @Nullable Pair<Context, PsiDirectory> maybeContext(
            @Nullable String moduleName,
            TLAplusDocument document) {
        if (moduleName == null) {
            return null;
        }

        VirtualFile virtualFile = document.file().getVirtualFile();
        if (virtualFile == null) {
            return null;
        }

        PsiDirectory directory = document.file().getContainingDirectory();
        if (directory == null) {
            return null;
        }

        Path path = virtualFile.getFileSystem().getNioPath(directory.getVirtualFile());
        if (path == null) {
            return null;
        }

        return Pair.pair(new Context(moduleName, path), directory);
    }
}
