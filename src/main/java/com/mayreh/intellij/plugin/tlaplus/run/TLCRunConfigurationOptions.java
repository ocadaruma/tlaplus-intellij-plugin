package com.mayreh.intellij.plugin.tlaplus.run;

import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class TLCRunConfigurationOptions extends LocatableRunConfigurationOptions {
    private final StoredProperty<String> workingDirectory = string("")
            .provideDelegate(this, "workingDirectory");

    private final StoredProperty<String> file = string("")
            .provideDelegate(this, "file");

    public String getWorkingDirectory() {
        return workingDirectory.getValue(this);
    }

    public void setWorkingDirectory(String value) {
        workingDirectory.setValue(this, value);
    }

    public String getFile() {
        return file.getValue(this);
    }

    public void setFile(String value) {
        file.setValue(this, value);
    }
}
