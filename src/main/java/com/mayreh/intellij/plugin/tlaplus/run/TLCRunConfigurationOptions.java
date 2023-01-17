package com.mayreh.intellij.plugin.tlaplus.run;

import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class TLCRunConfigurationOptions extends LocatableRunConfigurationOptions {
    private final StoredProperty<String> workingDirectory = string("")
            .provideDelegate(this, "workingDirectory");

    private final StoredProperty<String> file = string("")
            .provideDelegate(this, "file");

    private final StoredProperty<String> tlcArguments = string("")
            .provideDelegate(this, "tlcArguments");

    private final StoredProperty<String> jvmOptions = string("")
            .provideDelegate(this, "jvmOptions");

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

    public String getTlcArguments() {
        return tlcArguments.getValue(this);
    }

    public void setTlcArguments(String value) {
        tlcArguments.setValue(this, value);
    }

    public String getJvmOptions() {
        return jvmOptions.getValue(this);
    }

    public void setJvmOptions(String value) {
        jvmOptions.setValue(this, value);
    }
}
