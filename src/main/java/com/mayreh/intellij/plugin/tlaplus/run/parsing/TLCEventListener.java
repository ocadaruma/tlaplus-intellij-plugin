package com.mayreh.intellij.plugin.tlaplus.run.parsing;

/**
 * Interface to listen {@link TLCEvent} which fires upon {@link TLCEventParser}
 * detects some changes based on TLC's stdout
 */
@FunctionalInterface
public interface TLCEventListener {
    void onEvent(TLCEvent event);
}
