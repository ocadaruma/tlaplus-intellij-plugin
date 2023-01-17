package com.mayreh.intellij.plugin.tlaplus.run;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.actions.ConsolePropertiesProvider;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SimpleJavaSdkType;
import com.intellij.openapi.projectRoots.ex.PathUtilEx;
import com.intellij.util.SystemProperties;
import com.mayreh.intellij.plugin.tlaplus.run.ui.TLCOutputConsoleView;
import com.mayreh.intellij.plugin.tlaplus.run.ui.TLCSettingsEditor;
import com.mayreh.intellij.plugin.tlaplus.run.ui.TLCTestConsoleProperties;

import lombok.RequiredArgsConstructor;

public class TLCRunConfiguration extends LocatableConfigurationBase<TLCRunConfigurationOptions>
        implements ConsolePropertiesProvider {
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

    public String getTlcArguments() {
        return options().getTlcArguments();
    }

    public void setTlcArguments(String value) {
        options().setTlcArguments(value);
    }

    public String getJvmOptions() {
        return options().getJvmOptions();
    }

    public void setJvmOptions(String value) {
        options().setJvmOptions(value);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new TLCSettingsEditor();
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor,
                                              @NotNull ExecutionEnvironment environment)
            throws ExecutionException {
        TLCRunConfiguration that = this;
        return new CommandLineState(environment) {
            @Override
            protected @NotNull ProcessHandler startProcess() throws ExecutionException {
                setConsoleBuilder(new TLCConsoleBuilder(that, executor));

                SimpleJavaParameters params = new SimpleJavaParameters();

                Sdk jdk = PathUtilEx.getAnyJdk(getProject());
                if (jdk == null) {
                    jdk = SimpleJavaSdkType.getInstance().createJdk(
                            "tmp",
                            SystemProperties.getJavaHome());
                }

                params.setJdk(jdk);
                params.getClassPath().add(PathManager.getJarPathForClass(tlc2.TLC.class));
                params.setWorkingDirectory(getWorkingDirectory());
                params.getVMParametersList().addParametersString(getJvmOptions());
                params.setMainClass("tlc2.TLC");

                ParametersList args = params.getProgramParametersList();
                args.add("-tool");
                args.add("-modelcheck");

                ParametersList tlcArguments = new ParametersList();
                tlcArguments.addParametersString(getTlcArguments());
                if (!tlcArguments.hasParameter("-coverage")) {
                    tlcArguments.add("-coverage", "1");
                }
                args.addAll(tlcArguments.getParameters());
                args.add(getFile());

                GeneralCommandLine commandLine = params.toCommandLine();

                ProcessHandlerFactory factory = ProcessHandlerFactory.getInstance();
                OSProcessHandler handler = factory.createProcessHandler(commandLine);
                ProcessTerminatedListener.attach(handler);
                return handler;
            }
        };
    }

    @Override
    public @Nullable TestConsoleProperties createTestConsoleProperties(@NotNull Executor executor) {
        return null;
    }

    @RequiredArgsConstructor
    public static class TLCConsoleBuilder extends TextConsoleBuilder {
        private final TLCRunConfiguration configuration;
        private final Executor executor;
        private final List<Filter> filters = new ArrayList<>();

        @Override
        public @NotNull ConsoleView getConsole() {
            TLCOutputConsoleView console = new TLCOutputConsoleView(
                    new TLCTestConsoleProperties(configuration, executor));
            filters.forEach(console::addMessageFilter);
            return console;
        }

        @Override
        public void addFilter(@NotNull Filter filter) {
            filters.add(filter);
        }

        @Override
        public void setViewer(boolean isViewer) {
            // noop
        }
    }
}
