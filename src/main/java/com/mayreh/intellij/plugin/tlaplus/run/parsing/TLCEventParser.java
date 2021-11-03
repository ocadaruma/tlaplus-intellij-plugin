package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.intellij.util.Range;

import lombok.Value;
import lombok.experimental.Accessors;
import tlc2.output.EC;
import tlc2.output.MP;

public abstract class TLCEventParser {
    private static final Pattern STARTMSG_PATTERN = Pattern.compile("@!@!@STARTMSG (\\d+)(:\\d+)? @!@!@");
    private static final Pattern ENDMSG_PATTERN = Pattern.compile("@!@!@ENDMSG .* @!@!@");
    private static final Pattern DATETIME_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})");
    private static final Pattern SUCCESS_PATTERN = Pattern.compile("calculated \\(optimistic\\):\\s+val = ([-+.0-9Ee]+)");
    private static final Pattern FINISH_PATTERN = Pattern.compile("Finished in (\\d+)ms at \\((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\)");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    // TLA+ toolbox assumes only subset of errors to be shown as "Error"s
    // refs: https://github.com/tlaplus/tlaplus/blob/v1.7.1/toolbox/org.lamport.tla.toolbox.tool.tlc.ui/src/org/lamport/tla/toolbox/tool/tlc/output/data/TLCModelLaunchDataProvider.java#L323-L342
    private static final Set<Integer> interestedSeverities = new HashSet<>(
            Arrays.asList(MP.WARNING, MP.ERROR, MP.TLCBUG));
    private static final Set<Integer> ignorableCodes = new HashSet<>(
            Arrays.asList(
                    EC.TLC_BEHAVIOR_UP_TO_THIS_POINT,
                    EC.TLC_COUNTER_EXAMPLE,
                    EC.TLC_FEATURE_UNSUPPORTED,
                    EC.TLC_FEATURE_UNSUPPORTED_LIVENESS_SYMMETRY,
                    EC.TLC_FEATURE_LIVENESS_CONSTRAINTS
            ));

    /**
     * Add output line then maybe returns new {@link TLCEventParser} or as-is
     */
    public abstract TLCEventParser addLine(String line);

    public static TLCEventParser create(TLCEventListener listener) {
        return new Default(listener);
    }

    public void notifyProcessExit(int exitCode) {
        listener.onEvent(new TLCEvent.ProcessTerminated(exitCode));
    }

    protected final TLCEventListener listener;

    protected TLCEventParser(TLCEventListener listener) {
        this.listener = listener;
    }

    @Value
    @Accessors(fluent = true)
    static class Message {
        int code;
        @Nullable
        Integer severity;
    }

    protected Optional<Message> parseStartMessage(String line) {
        Matcher matcher = STARTMSG_PATTERN.matcher(line);
        if (matcher.find()) {
            int code = Integer.valueOf(matcher.group(1));
            Integer severity = null;
            if (matcher.group(2) != null) {
                severity = Integer.valueOf(matcher.group(2).substring(1));
            }
            return Optional.of(new Message(code, severity));
        }
        return Optional.empty();
    }

    protected boolean parseEndMessage(String line) {
        Matcher matcher = ENDMSG_PATTERN.matcher(line);
        return matcher.find();
    }

    static class Default extends TLCEventParser {
        Default(TLCEventListener listener) {
            super(listener);
        }

        @Override
        public TLCEventParser addLine(String line) {
            return parseStartMessage(line).map(msg -> {
                if (interestedSeverities.contains(msg.severity) &&
                    !ignorableCodes.contains(msg.code)) {
                    return new MultilineTextParser<TLCEvent>(
                            listener, lines -> new TLCEvent.TLCError(String.join("", lines)));
                }
                switch (msg.code()) {
                    case EC.TLC_MODE_MC:
                        return new MultilineTextParser<>(listener, TLCEvent.MC::new);
                    case EC.TLC_SANY_START:
                        return new SANYParser(listener);
                    case EC.TLC_STARTING:
                        return new MultilineTextParser<>(listener, lines -> {
                            Matcher matcher = mustMatch(DATETIME_PATTERN, String.join(" ", lines));
                            return new TLCEvent.TLCStart(LocalDateTime.parse(matcher.group(1), DATETIME_FORMAT));
                        });
                    case EC.TLC_CHECKPOINT_START:
                        return new MultilineTextParser<>(
                                listener, ignore -> TLCEvent.CheckpointStart.INSTANCE);
                    case EC.TLC_COMPUTING_INIT:
                    case EC.TLC_COMPUTING_INIT_PROGRESS:
                        return new MultilineTextParser<>(
                                listener, ignore -> TLCEvent.InitialStatesComputing.INSTANCE);
                    case EC.TLC_INIT_GENERATED1:
                    case EC.TLC_INIT_GENERATED2:
                    case EC.TLC_INIT_GENERATED3:
                    case EC.TLC_INIT_GENERATED4:
                        return new MultilineTextParser<>(listener, ProgressParser::parseInit);
                    case EC.TLC_CHECKING_TEMPORAL_PROPS:
                        return new MultilineTextParser<>(listener, lines -> {
                            if (String.join(" ", lines).contains("complete")) {
                                return TLCEvent.CheckingLivenessFinal.INSTANCE;
                            }
                            return TLCEvent.CheckingLiveness.INSTANCE;
                        });
                    case EC.TLC_PROGRESS_STATS:
                        return new MultilineTextParser<>(listener, ProgressParser::parse);
                    case EC.TLC_COVERAGE_INIT:
                        return new MultilineTextParser<>(listener, lines ->
                                new TLCEvent.CoverageInit(CoverageItemParser.parse(lines)));
                    case EC.TLC_COVERAGE_NEXT:
                        return new MultilineTextParser<>(listener, lines ->
                                new TLCEvent.CoverageNext(CoverageItemParser.parse(lines)));
                    case EC.TLC_STATE_PRINT1:
                    case EC.TLC_STATE_PRINT2:
                    case EC.TLC_STATE_PRINT3:
                    case EC.TLC_BACK_TO_STATE:
                        return new MultilineTextParser<>(listener, TLCEvent.ErrorTrace::new);
                    case EC.TLC_SUCCESS:
                        return new MultilineTextParser<>(listener, lines -> {
                            Matcher matcher = mustMatch(SUCCESS_PATTERN, String.join(" ", lines));
                            return new TLCEvent.TLCSuccess(Double.valueOf(matcher.group(1)));
                        });
                    case EC.TLC_FINISHED:
                        return new MultilineTextParser<>(listener, lines -> {
                            Matcher matcher = mustMatch(FINISH_PATTERN, String.join(" ", lines));
                            return new TLCEvent.TLCFinished(
                                    Duration.ofMillis(Long.valueOf(matcher.group(1))),
                                    LocalDateTime.parse(matcher.group(2), DATETIME_FORMAT));
                        });
                    default:
                        return new Other(listener);
                }
            }).orElseGet(() -> {
                listener.onEvent(new TLCEvent.TextEvent(line));
                return this;
            });
        }
    }

    static class SANYParser extends TLCEventParser {
        private final List<String> sanyLines = new ArrayList<>();

        enum State { Init, Running, Ending, }
        private State state = State.Init;

        SANYParser(TLCEventListener listener) {
            super(listener);
        }

        @Override
        public TLCEventParser addLine(String line) {
            switch (state) {
                case Init:
                    if (parseEndMessage(line)) {
                        state = State.Running;
                        listener.onEvent(TLCEvent.SANYStart.INSTANCE);
                    } else {
                        listener.onEvent(new TLCEvent.TextEvent(line));
                    }
                    return this;
                case Running:
                    Optional<Message> msg = parseStartMessage(line);
                    if (msg.isPresent() && msg.get().code == EC.TLC_SANY_END) {
                        state = State.Ending;
                    } else {
                        sanyLines.add(line);
                    }
                    return this;
                case Ending:
                    if (parseEndMessage(line)) {
                        listener.onEvent(new TLCEvent.SANYEnd(sanyLines));
                        return new Default(listener);
                    } else {
                        listener.onEvent(new TLCEvent.TextEvent(line));
                        return this;
                    }
                default:
                    throw new RuntimeException("Never");
            }
        }
    }

    private abstract static class StatefulParser extends TLCEventParser {
        StatefulParser(TLCEventListener listener) {
            super(listener);
        }

        @Override
        public TLCEventParser addLine(String line) {
            if (parseEndMessage(line)) {
                finish();
                return new Default(listener);
            } else {
                processLine(line);
                return this;
            }
        }

        protected abstract void processLine(String line);
        protected abstract void finish();
    }

    static class MultilineTextParser<T extends TLCEvent> extends StatefulParser {
        private final Function<List<String>, T> createEvent;
        private final List<String> lines = new ArrayList<>();

        MultilineTextParser(TLCEventListener listener,
                            Function<List<String>, T> createEvent) {
            super(listener);
            this.createEvent = createEvent;
        }

        @Override
        protected void processLine(String line) {
            lines.add(line);
        }

        @Override
        protected void finish() {
            listener.onEvent(createEvent.apply(lines));
        }
    }

    static class Other extends StatefulParser {
        Other(TLCEventListener listener) {
            super(listener);
        }

        @Override
        protected void processLine(String line) {
            listener.onEvent(new TLCEvent.TextEvent(line));
        }

        @Override
        protected void finish() {
            // noop
        }
    }

    private static class ProgressParser {
        private static final Pattern INIT_PATTERN = Pattern.compile(
                "Finished computing initial states: (\\d+) distinct states? generated at (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}).*");
        private static final Pattern PATTERN = Pattern.compile(
                "Progress\\(([\\d,]+)\\) at (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}): ([\\d,]+) states generated.*, ([\\d,]+) distinct states found.*, ([\\d,]+) states left on queue.*");

        static TLCEvent.Progress parseInit(List<String> lines) {
            Matcher matcher = mustMatch(INIT_PATTERN, String.join("", lines));
            int count = toInt(matcher.group(1));
            LocalDateTime timestamp = LocalDateTime.parse(matcher.group(2), DATETIME_FORMAT);
            return new TLCEvent.Progress(timestamp, 0, count, count, count);
        }

        static TLCEvent.Progress parse(List<String> lines) {
            Matcher matcher = mustMatch(PATTERN, String.join("", lines));
            LocalDateTime timestamp = LocalDateTime.parse(matcher.group(2), DATETIME_FORMAT);
            int diameter = toInt(matcher.group(1));
            int total = toInt(matcher.group(3));
            int distinct = toInt(matcher.group(4));
            int queueSize = toInt(matcher.group(5));
            return new TLCEvent.Progress(timestamp, diameter, total, distinct, queueSize);
        }
    }

    private static class CoverageItemParser {
        private static final Pattern PATTERN = Pattern.compile(
                "<(\\w+) line (\\d+), col (\\d+) to line (\\d+), col (\\d+) of module (\\w+)>: (\\d+):(\\d+)");

        static TLCEvent.CoverageItem parse(List<String> lines) {
            Matcher matcher = mustMatch(PATTERN, String.join("", lines));
            String moduleName = matcher.group(6);
            String actionName = matcher.group(1);

            // translating to 0-origin
            SourceLocation start = new SourceLocation(
                    toInt(matcher.group(2)) - 1, toInt(matcher.group(3)) - 1);
            // column should be exclusive so no need to subtract 1
            SourceLocation endExclusive = new SourceLocation(
                    toInt(matcher.group(4)) - 1, toInt(matcher.group(5)));

            int distinct = toInt(matcher.group(7));
            int total = toInt(matcher.group(8));
            return new TLCEvent.CoverageItem(moduleName, actionName, total, distinct, new Range<>(start, endExclusive));
        }
    }

    private static Matcher mustMatch(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        boolean matched = matcher.find();
        assert matched : "Input " + input + " must match to the pattern " + pattern;
        return matcher;
    }

    private static int toInt(String s) {
        return Integer.parseInt(s.replace(",", ""));
    }
}
