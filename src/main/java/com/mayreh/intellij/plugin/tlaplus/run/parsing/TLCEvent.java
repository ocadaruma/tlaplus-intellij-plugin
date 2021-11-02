package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.time.Duration;
import java.time.LocalDateTime;
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
    class MC implements TLCEvent {
        List<String> lines;
    }

    class SANYStart implements TLCEvent {
        public static final SANYStart INSTANCE = new SANYStart();
    }

    class SANYEnd implements TLCEvent {
        public SANYEnd(List<String> sanyLines) {
        }
    }

    @Value
    @Accessors(fluent = true)
    class TLCStart implements TLCEvent {
        LocalDateTime startedAt;
    }

    class CheckpointStart implements TLCEvent {
        public static final CheckpointStart INSTANCE = new CheckpointStart();
    }

    class InitialStatesComputing implements TLCEvent {
        public static final InitialStatesComputing INSTANCE = new InitialStatesComputing();
    }

    class InitialStatesComputed implements TLCEvent {
        public InitialStatesComputed(List<String> lines) {
        }
    }

    class CheckingLiveness implements TLCEvent {
        public static final CheckingLiveness INSTANCE = new CheckingLiveness();
    }

    class CheckingLivenessFinal implements TLCEvent {
        public static final CheckingLivenessFinal INSTANCE = new CheckingLivenessFinal();
    }

    class Progress implements TLCEvent {
        public Progress(List<String> lines) {
        }
    }

    class ErrorTrace implements TLCEvent {
        public ErrorTrace(List<String> lines) {
        }
    }

    @Value
    @Accessors(fluent = true)
    class Success implements TLCEvent {
        double fingerprintCollisionProbability;
    }

    @Value
    @Accessors(fluent = true)
    class Finished implements TLCEvent {
        Duration duration;
        LocalDateTime finishedAt;
    }
}
