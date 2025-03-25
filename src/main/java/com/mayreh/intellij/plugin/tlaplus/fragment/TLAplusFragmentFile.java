package com.mayreh.intellij.plugin.tlaplus.fragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.TLAplusLanguage;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.run.eval.DummyModule;
import com.mayreh.intellij.plugin.util.TLAplusTreeUtil;

public class TLAplusFragmentFile extends PsiFileBase {
    /**
     * Store dummy module that this code fragment will be embedded to
     * so that it can be referred on reference resolution.
     */
    private static final Key<TLAplusModule> MODULE_KEY =
            Key.create("TLA.fragment.module");

    public TLAplusFragmentFile(@NotNull FileViewProvider viewProvider,
                               @NotNull Language language) {
        super(viewProvider, language);
    }

    public void setModule(@NotNull TLAplusModule module) {
        putCopyableUserData(MODULE_KEY, module);
    }

    public @Nullable TLAplusModule module() {
        return getCopyableUserData(MODULE_KEY);
    }

    @Override
    public @NotNull FileType getFileType() {
        return TLAplusFragmentFileType.INSTANCE;
    }

    public static PsiFile createFragmentFile(Project project, String text) {
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
}
