package com.mayreh.intellij.plugin.tlaplus.run;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;

public class TLCConfigurationFactory extends ConfigurationFactory {
    TLCConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return TLCRunConfigurationType.ID;
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new TLCRunConfiguration(project, this, "TLC");
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return TLCRunConfigurationOptions.class;
    }
}
