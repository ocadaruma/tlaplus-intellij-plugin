package com.mayreh.intellij.plugin.tlaplus.run;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.mayreh.intellij.plugin.icons.TLAplusIcons;

public class TLCRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "TLC";

    TLCRunConfigurationType() {
        super("TLCRunConfiguration",
              ID,
                "TLC run configuration",
              TLAplusIcons.TLA_PLUS);

        addFactory(new TLCConfigurationFactory(this));
    }

    public static TLCRunConfigurationType instance() {
        return ConfigurationTypeUtil.findConfigurationType(TLCRunConfigurationType.class);
    }
}
