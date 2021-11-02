package com.mayreh.intellij.plugin.tlaplus.run.parsing;

public interface TLCEventListener {
    void onEvent(TLCEvent event);

    void onProcessExit(int exitCode);
}
