package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.config.Storage;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.run.TLCRunConfiguration;

public class TLCTestConsoleProperties extends TestConsoleProperties {
    private final TLCRunConfiguration configuration;

    public TLCTestConsoleProperties(TLCRunConfiguration configuration,
                                    Executor executor) {
        super(new Storage.PropertiesComponentStorage(
                "TLCSupport.",
                PropertiesComponent.getInstance()), configuration.getProject(), executor);
        this.configuration = configuration;
    }

    @Override
    public @NotNull ConsoleView createConsole() {
        return new TextConsoleBuilderImpl(configuration.getProject()).getConsole();
    }

    @Override
    public RunProfile getConfiguration() {
        return configuration;
    }

    public @Nullable TLAplusModule module() {
        String filePath = configuration.getFile();
        if (filePath == null) {
            return null;
        }

        VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(Paths.get(filePath));
        if (file == null) {
            return null;
        }

        PsiFile psiFile = PsiManager.getInstance(configuration.getProject())
                                    .findFile(file);
        if (psiFile instanceof TLAplusFile) {
            return PsiTreeUtil.findChildOfType(psiFile, TLAplusModule.class);
        }

        return null;
    }
}
