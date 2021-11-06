package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.intellij.util.Range;

import lombok.ToString;
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

    @ToString
    enum SANYStart implements TLCEvent {
        INSTANCE
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

    @ToString
    enum CheckpointStart implements TLCEvent {
        INSTANCE
    }

    @ToString
    enum InitialStatesComputing implements TLCEvent {
        INSTANCE
    }

    @ToString
    enum CheckingLiveness implements TLCEvent {
        INSTANCE
    }

    @ToString
    enum CheckingLivenessFinal implements TLCEvent {
        INSTANCE
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

    @Value
    @Accessors(fluent = true)
    class TLCError implements TLCEvent {
        public enum Severity {
            Warning, Error,
        }

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

        Severity severity;
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

    interface ErrorTraceEvent extends TLCEvent {
        @Value
        @Accessors(fluent = true)
        class SimpleErrorTrace implements ErrorTraceEvent {
            int number;
            String module;
            String action;
            Range<SourceLocation> range;
            List<TraceVariable> variables;
        }

        @Value
        @Accessors(fluent = true)
        class SpecialErrorTrace implements ErrorTraceEvent {
            int number;
            String type;
            List<TraceVariable> variables;
        }

        @Value
        @Accessors(fluent = true)
        class BackToStateErrorTrace implements ErrorTraceEvent {
            int number;
            String module;
            String action;
            Range<SourceLocation> range;
            List<TraceVariable> variables;
        }

        @Value
        @Accessors(fluent = true)
        class TraceVariable {
            String name;
            TraceVariableValue value;
        }

        interface TraceVariableValue {}

        @Value
        @Accessors(fluent = true)
        class PrimitiveValue implements TraceVariableValue {
            String content;
        }

        @Value
        @Accessors(fluent = true)
        class SequenceValue implements TraceVariableValue {
            List<TraceVariableValue> values;
        }

        @Value
        @Accessors(fluent = true)
        class SetValue implements TraceVariableValue {
            // We use list here though the value type is "Set" to preserve
            // output from TLC
            List<TraceVariableValue> values;
        }

        @Value
        @Accessors(fluent = true)
        class RecordValue implements TraceVariableValue {
            @Value
            @Accessors(fluent = true)
            public static class Entry {
                String key;
                TraceVariableValue value;
            }

            List<Entry> entries;
        }

        @Value
        @Accessors(fluent = true)
        class FunctionValue implements TraceVariableValue {
            @Value
            @Accessors(fluent = true)
            public static class Entry {
                String key;
                TraceVariableValue value;
            }

            List<Entry> entries;
        }

        // Should not happen in principle.
        // Just a fallback type because currently we're not 100% sure about
        // TLC's output spec
        @Value
        @Accessors(fluent = true)
        class UnknownValue implements TraceVariableValue {
            String text;
        }
    }
}
