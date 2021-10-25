package com.mayreh.intellij.plugin.tlaplus.run;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.mayreh.intellij.plugin.tlaplus.TLAplusFile;

public class TLCRunConfigurationProducer extends LazyRunConfigurationProducer<TLCRunConfiguration> {
    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return TLCRunConfigurationType.instance().getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull TLCRunConfiguration configuration,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        VirtualFile file = maybeVirtualFile(context);
        if (file != null) {
            configuration.setFile(file.getCanonicalPath());
            configuration.setWorkingDirectory(file.toNioPath().getParent().toString());
            configuration.setName(file.getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull TLCRunConfiguration configuration,
                                              @NotNull ConfigurationContext context) {
        VirtualFile file = maybeVirtualFile(context);
        if (file != null) {
            return Objects.equals(file.getCanonicalPath(), configuration.getFile()) &&
                   Objects.equals(file.toNioPath().getParent().toString(), configuration.getWorkingDirectory());
        }
        return false;
    }

    private VirtualFile maybeVirtualFile(ConfigurationContext context) {
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
