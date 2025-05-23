package com.mayreh.intellij.plugin.tlaplus.run;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;
import com.mayreh.intellij.plugin.tlaplus.run.TLCBeforeRunTaskProvider.TLCBeforeRunTask;

public class TLCRunConfigurationProducer extends LazyRunConfigurationProducer<TLCRunConfiguration> {
    @Override
    public @NotNull ConfigurationFactory getConfigurationFactory() {
        return TLCRunConfigurationType.instance().getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull TLCRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        VirtualFile file = maybeVirtualFile(context);
        if (file == null) {
            return false;
        }

        Path path = file.getFileSystem().getNioPath(file);
        if (path == null) {
            return false;
        }

        configuration.setFile(file.getCanonicalPath());
        configuration.setWorkingDirectory(path.getParent().toString());
        configuration.setName(file.getName());
        configuration.setBeforeRunTasks(Collections.singletonList(new TLCBeforeRunTask()));
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull TLCRunConfiguration configuration,
                                              @NotNull ConfigurationContext context) {
        VirtualFile file = maybeVirtualFile(context);
        if (file == null) {
            return false;
        }

        Path path = file.getFileSystem().getNioPath(file);
        if (path == null) {
            return false;
        }

        return Objects.equals(file.getCanonicalPath(), configuration.getFile()) &&
               Objects.equals(path.getParent().toString(), configuration.getWorkingDirectory());
    }

    private static @Nullable VirtualFile maybeVirtualFile(ConfigurationContext context) {
        Location location = context.getLocation();
        if (location == null) {
            return null;
        }
        VirtualFile file = location.getVirtualFile();
        if (file == null) {
            return null;
        }
        if (!(PsiManager.getInstance(context.getProject()).findFile(file) instanceof TLAplusFile)) {
            return null;
        }
        return file;
    }
}
