package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.util.List;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Interface that all TLC output events must inherit
 */
public interface TLCEvent {
    @Value
    @Accessors(fluent = true)
    class TextEvent implements TLCEvent {
        String text;
    }

    @Value
    @Accessors(fluent = true)
    class SANYStart implements TLCEvent {
        List<String> messages;
    }

    @Value
    @Accessors(fluent = true)
    class SANYEnd implements TLCEvent {
        List<String> messages;
    }
}
