package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CheckingLiveness;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CheckingLivenessFinal;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CheckpointStart;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.InitialStatesComputed;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.InitialStatesComputing;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.MC;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.Progress;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCStart;

import lombok.Value;
import lombok.experimental.Accessors;
import tlc2.output.EC;

public abstract class TLCEventParser {
    private static final Pattern STARTMSG_PATTERN = Pattern.compile("@!@!@STARTMSG (\\d+)(:\\d+)? @!@!@");
    private static final Pattern ENDMSG_PATTERN = Pattern.compile("@!@!@ENDMSG .* @!@!@");
    private static final Pattern DATETIME_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})");
    private static final Pattern SUCCESS_PATTERN = Pattern.compile("calculated \\(optimistic\\):\\s+val = ([-+0-9Ee]+)");
    private static final Pattern FINISH_PATTERN = Pattern.compile("Finished in ([0-9]+)ms at \\((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\)");

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    /**
     * Add output line then maybe returns new {@link TLCEventParser} or as-is
     */
    public abstract TLCEventParser addLine(String line);

    public static TLCEventParser create(TLCEventListener listener) {
        return new Default(listener);
    }

    public void notifyProcessExit(int exitCode) {
        listener.onProcessExit(exitCode);
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
                switch (msg.code()) {
                    case EC.TLC_MODE_MC:
                        return new MultilineTextParser<>(listener, MC::new);
                    case EC.TLC_SANY_START:
                        return new SANYParser(listener);
                    case EC.TLC_STARTING:
                        return new MultilineTextParser<>(listener, lines -> {
                            Matcher matcher = DATETIME_PATTERN.matcher(String.join(" ", lines));
                            matcher.find();
                            return new TLCStart(LocalDateTime.parse(matcher.group(1), DATETIME_FORMAT));
                        });
                    case EC.TLC_CHECKPOINT_START:
                        return new MultilineTextParser<>(
                                listener, ignore -> CheckpointStart.INSTANCE);
                    case EC.TLC_COMPUTING_INIT:
                    case EC.TLC_COMPUTING_INIT_PROGRESS:
                        return new MultilineTextParser<>(
                                listener, ignore -> InitialStatesComputing.INSTANCE);
                    case EC.TLC_INIT_GENERATED1:
                    case EC.TLC_INIT_GENERATED2:
                    case EC.TLC_INIT_GENERATED3:
                    case EC.TLC_INIT_GENERATED4:
                        return new MultilineTextParser<>(listener, InitialStatesComputed::new);
                    case EC.TLC_CHECKING_TEMPORAL_PROPS:
                        return new MultilineTextParser<>(listener, lines -> {
                            if (String.join(" ", lines).contains("complete")) {
                                return CheckingLivenessFinal.INSTANCE;
                            }
                            return CheckingLiveness.INSTANCE;
                        });
                    case EC.TLC_PROGRESS_STATS:
                        return new MultilineTextParser<>(listener, Progress::new);
                    case EC.TLC_STATE_PRINT1:
                    case EC.TLC_STATE_PRINT2:
                    case EC.TLC_STATE_PRINT3:
                    case EC.TLC_BACK_TO_STATE:
                        return new MultilineTextParser<>(listener, ErrorTrace::new);
                    case EC.TLC_SUCCESS:
                        return new MultilineTextParser<>(listener, lines -> {
                            Matcher matcher = SUCCESS_PATTERN.matcher(String.join(" ", lines));
                            matcher.find();
                            return new TLCEvent.Success(Double.valueOf(matcher.group(1)));
                        });
                    case EC.TLC_FINISHED:
                        return new MultilineTextParser<>(listener, lines -> {
                            Matcher matcher = FINISH_PATTERN.matcher(String.join(" ", lines));
                            matcher.find();
                            return new TLCEvent.Finished(
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
}
