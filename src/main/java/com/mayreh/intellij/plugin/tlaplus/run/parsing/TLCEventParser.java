package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tlc2.output.EC;

public abstract class TLCEventParser {
    /**
     * Add output line then maybe returns new {@link TLCEventParser} or as-is
     */
    public abstract TLCEventParser addLine(String line);

    protected final TLCEventListener listener;

    protected TLCEventParser(TLCEventListener listener) {
        this.listener = listener;
    }

    public static class Default extends TLCEventParser {
        private static final Pattern START_PATTERN = Pattern.compile("@!@!@STARTMSG (\\d+)(:\\d+)? @!@!@");

        public Default(TLCEventListener listener) {
            super(listener);
        }

        @Override
        public TLCEventParser addLine(String line) {
            Matcher matcher = START_PATTERN.matcher(line);
            if (matcher.find()) {
                int code = Integer.valueOf(matcher.group(1));
                switch (code) {
                    case EC.TLC_SANY_START:
                        return new MultilineTextParser<>(listener, TLCEvent.SANYStart::new);
                    case EC.TLC_SANY_END:
                        return new MultilineTextParser<>(listener, TLCEvent.SANYEnd::new);
                    default:
                        return new Other(listener);
                }
            } else {
                listener.onEvent(new TLCEvent.TextEvent(line));
                return this;
            }
        }
    }

    private abstract static class StatefulParser extends TLCEventParser {
        private static final Pattern END_PATTERN = Pattern.compile("@!@!@ENDMSG .* @!@!@");

        StatefulParser(TLCEventListener listener) {
            super(listener);
        }

        @Override
        public TLCEventParser addLine(String line) {
            if (END_PATTERN.matcher(line).find()) {
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
        private final List<String> messages = new ArrayList<>();

        MultilineTextParser(TLCEventListener listener,
                            Function<List<String>, T> createEvent) {
            super(listener);
            this.createEvent = createEvent;
        }

        @Override
        protected void processLine(String line) {
            messages.add(line);
        }

        @Override
        protected void finish() {
            listener.onEvent(createEvent.apply(messages));
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
