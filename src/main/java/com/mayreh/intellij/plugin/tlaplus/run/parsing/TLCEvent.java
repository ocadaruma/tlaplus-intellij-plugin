package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.intellij.util.Range;

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

    @Value
    @Accessors(fluent = true)
    class SANYEnd implements TLCEvent {
        List<SANYError> errors;
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

    class CheckingLiveness implements TLCEvent {
        public static final CheckingLiveness INSTANCE = new CheckingLiveness();
    }

    class CheckingLivenessFinal implements TLCEvent {
        public static final CheckingLivenessFinal INSTANCE = new CheckingLivenessFinal();
    }

    @Value
    @Accessors(fluent = true)
    class Progress implements TLCEvent {
        LocalDateTime timestamp;
        int diameter;
        int total;
        int distinct;
        int queueSize;
    }

    @Value
    @Accessors(fluent = true)
    class CoverageInit implements TLCEvent {
        CoverageItem item;
    }

    @Value
    @Accessors(fluent = true)
    class CoverageNext implements TLCEvent {
        CoverageItem item;
    }

    @Value
    @Accessors(fluent = true)
    class CoverageItem {
        String module;
        String action;
        int total;
        int distinct;
        Range<SourceLocation> range;
    }

    class ErrorTrace implements TLCEvent {
        public ErrorTrace(List<String> lines) {
        }
    }

    @Value
    @Accessors(fluent = true)
    class TLCError implements TLCEvent {
        public interface ErrorItem {
            String message();
        }

        @Value
        @Accessors(fluent = true)
        public static class LocatableErrorItem implements ErrorItem {
            String module;
            SourceLocation location;
            String message;
        }

        @Value
        @Accessors(fluent = true)
        public static class SimpleErrorItem implements ErrorItem {
            String message;
        }

        List<ErrorItem> errors;
    }

    @Value
    @Accessors(fluent = true)
    class TLCSuccess implements TLCEvent {
        double fingerprintCollisionProbability;
    }

    @Value
    @Accessors(fluent = true)
    class TLCFinished implements TLCEvent {
        Duration duration;
        LocalDateTime finishedAt;
    }

    @Value
    @Accessors(fluent = true)
    class ProcessTerminated implements TLCEvent {
        int exitCode;
    }

    @Value
    @Accessors(fluent = true)
    class SANYError {
        String module;
        SourceLocation location;
        String message;
    }
}
