package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.SANYError;

import tlc2.output.EC;

class SANYEventParser extends TLCEventParser {
    private static final Pattern PARSE_ERROR = Pattern.compile("\\bat line (\\d+), col(?:umn)? (\\d+)\\s+.*");
    private static final Pattern LEXICAL_ERROR = Pattern.compile("\\s*Lexical error at line (\\d+), column (\\d+).\\s*(.*)");
    private static final Pattern SEMANTIC_ERROR = Pattern.compile("\\s*line (\\d+), col (\\d+) to line \\d+, col \\d+ of module \\w+\\s*");

    private final List<String> sanyLines = new ArrayList<>();

    enum State { Init, Running, Ending, }
    private State state = State.Init;

    SANYEventParser(TLCEventListener listener) {
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
                Optional<TLCMessage> msg = parseStartMessage(line);
                if (msg.isPresent() && msg.get().code() == EC.TLC_SANY_END) {
                    state = State.Ending;
                } else {
                    sanyLines.add(line);
                }
                return this;
            case Ending:
                if (parseEndMessage(line)) {
                    listener.onEvent(result(sanyLines));
                    return new Default(listener);
                } else {
                    listener.onEvent(new TLCEvent.TextEvent(line));
                    return this;
                }
            default:
                throw new RuntimeException("Never");
        }
    }

    private enum Mode {
        Parse,
        Semantic,
        ParseError,
        SemanticError,
        SemanticErrorDetail,
    }

    private static TLCEvent.SANYEnd result(List<String> sanyLines) {
        List<SANYError> errors = new ArrayList<>();

        String currentModule = null;
        Mode mode = null;
        SourceLocation lastLocation = null;
        for (String line : sanyLines) {
            line = line.trim();
            if (line.isBlank()) {
                continue;
            }

            /* State transitions */
            if (line.startsWith("Parsing file")) {
                int lastPathSeparatorIndex = IntStream.of(
                        line.lastIndexOf('/'),
                        line.lastIndexOf('\\'),
                        line.lastIndexOf('¥')).max().getAsInt();
                int extensionIndex = line.lastIndexOf(".tla");
                currentModule = line.substring(lastPathSeparatorIndex + 1, extensionIndex);
                mode = Mode.Parse;
                continue;
            }
            if (line.startsWith("Semantic processing of module ")) {
                currentModule = line.substring("Semantic processing of module ".length());
                mode = Mode.Semantic;
                continue;
            }
            if (line.startsWith("***Parse Error")) {
                mode = Mode.ParseError;
                continue;
            }
            if (mode == Mode.Semantic && line.startsWith("*** Errors")) {
                mode = Mode.SemanticError;
                continue;
            }

            if (mode == null) {
                continue;
            }
            /* Error parsing depending on current state */
            switch (mode) {
                case Parse: {
                    Matcher matcher = LEXICAL_ERROR.matcher(line);
                    if (matcher.find()) {
                        int lineNum = Integer.parseInt(matcher.group(1));
                        int col = Integer.parseInt(matcher.group(2));
                        errors.add(new SANYError(currentModule, new SourceLocation(lineNum, col), matcher.group(3)));
                    }
                    break;
                }
                case ParseError: {
                    Matcher matcher = PARSE_ERROR.matcher(line);
                    if (matcher.find()) {
                        int lineNum = Integer.parseInt(matcher.group(1));
                        int col = Integer.parseInt(matcher.group(2));
                        errors.add(new SANYError(currentModule, new SourceLocation(lineNum, col), line));
                    }
                    break;
                }
                case SemanticError: {
                    Matcher matcher = SEMANTIC_ERROR.matcher(line);
                    if (matcher.find()) {
                        int lineNum = Integer.parseInt(matcher.group(1));
                        int col = Integer.parseInt(matcher.group(2));
                        lastLocation = new SourceLocation(lineNum, col);
                        mode = Mode.SemanticErrorDetail;
                    }
                    break;
                }
                case SemanticErrorDetail: {
                    if (lastLocation != null) {
                        errors.add(new SANYError(currentModule, lastLocation, line));
                        lastLocation = null;
                        mode = Mode.SemanticError;
                    }
                    break;
                }
                default:
                    break;
            }
        }

        return new TLCEvent.SANYEnd(errors);
    }
}
