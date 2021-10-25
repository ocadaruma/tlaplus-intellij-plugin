package com.mayreh.intellij.plugin.tlaplus.run;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.icons.AllIcons.General;

public class TLCRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "TLC";

    TLCRunConfigurationType() {
        super("TLCRunConfiguration",
              ID,
                "TLC run configuration",
              General.Information);

        addFactory(new TLCConfigurationFactory(this));
    }

    public static TLCRunConfigurationType instance() {
        return ConfigurationTypeUtil.findConfigurationType(TLCRunConfigurationType.class);
    }
}
