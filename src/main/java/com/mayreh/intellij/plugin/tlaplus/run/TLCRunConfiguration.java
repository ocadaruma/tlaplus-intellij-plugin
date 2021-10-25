package com.mayreh.intellij.plugin.tlaplus.run;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.mayreh.intellij.plugin.tlaplus.run.ui.TLCSettingsEditor;

public class TLCRunConfiguration extends LocatableConfigurationBase<TLCRunConfigurationOptions> {
    TLCRunConfiguration(@NotNull Project project,
                        @NotNull ConfigurationFactory factory,
                        @Nullable String name) {
        super(project, factory, name);
    }

    private TLCRunConfigurationOptions options() {
        return (TLCRunConfigurationOptions) getOptions();
    }

    public String getWorkingDirectory() {
        return options().getWorkingDirectory();
    }

    public void setWorkingDirectory(String value) {
        options().setWorkingDirectory(value);
    }

    public String getFile() {
        return options().getFile();
    }

    public void setFile(String value) {
        options().setFile(value);
    }

    public String getConfigFile() {
        return options().getConfigFile();
    }

    public void setConfigFile(String value) {
        options().setConfigFile(value);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new TLCSettingsEditor();
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor,
                                              @NotNull ExecutionEnvironment environment)
            throws ExecutionException {
        return new JavaCommandLineState(environment) {
            @Override
            protected JavaParameters createJavaParameters() throws ExecutionException {
                JavaParameters params = new JavaParameters();
                params.setJdk(JavaParametersUtil.createProjectJdk(getProject(), null));
                params.getClassPath().add(PathManager.getJarPathForClass(tlc2.TLC.class));
                params.setWorkingDirectory(getWorkingDirectory());
                params.setMainClass("tlc2.TLC");

                ParametersList args = params.getProgramParametersList();
                args.add("-tool");
                args.add("-modelcheck");
                args.add("-coverage", "1");
                if (StringUtil.isNotEmpty(getConfigFile())) {
                    args.add("-config", getConfigFile());
                }
                args.add(getFile());
                return params;
            }
        };
    }
}
