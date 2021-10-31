package com.mayreh.intellij.plugin.tlaplus.run.ui;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.util.config.Storage;
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
}
