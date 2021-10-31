package com.mayreh.intellij.plugin.tlaplus.run.parsing;

@FunctionalInterface
public interface TLCEventListener {
    void onEvent(TLCEvent event);
}
