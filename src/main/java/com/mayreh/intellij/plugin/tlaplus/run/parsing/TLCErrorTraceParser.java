package com.mayreh.intellij.plugin.tlaplus.run.parsing;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.IDENTIFIER;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.MULTIPLE_VARIABLE_TRACE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.SINGLE_VARIABLE_TRACE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_ERROR_TRACE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_FUNCTION_PAIR;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_FUNCTION_VALUE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_PRIMITIVE_VALUE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_RECORD_PAIR;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_RECORD_VALUE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_SEQUENCE_EMPTY_VALUE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_SEQUENCE_VALUE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_SET_EMPTY_VALUE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_SET_VALUE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_VALUE;
import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes.TLC_VARIABLE_DECL;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Range;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.BackToStateErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.FunctionValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.PrimitiveValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.RecordValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SequenceValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SetValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SimpleErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SpecialErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariable;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.UnknownValue;

public class TLCErrorTraceParser {
    private static final Pattern ERROR_TRACE_PATTERN = Pattern.compile(
            "(\\d+): <(\\w+) line (\\d+), col (\\d+) to line (\\d+), col (\\d+) of module (\\w+)>");
    private static final Pattern SPECIAL_ERROR_TRACE_PATTERN = Pattern.compile(
            "(\\d+): <?([\\w\\s]+)>?");
    private static final Pattern BACK_TO_STATE_PATTERN = Pattern.compile(
            "(\\d+): Back to state: <(\\w+) line (\\d+), col (\\d+) to line (\\d+), col (\\d+) of module (\\w+)>");
    private static final TLCErrorTraceParserDefinition PARSER_DEFINITION = new TLCErrorTraceParserDefinition();

    public Optional<ErrorTraceEvent> parse(List<String> lines) {
        if (lines.isEmpty()) {
            return Optional.empty();
        }
        String firstLine = lines.get(0);
        final List<TraceVariable> variables;
        if (lines.size() > 1) {
            variables = parseVariables(String.join("\n", lines.subList(1, lines.size())));
        } else {
            variables = emptyList();
        }

        Matcher matcher;
        matcher = ERROR_TRACE_PATTERN.matcher(firstLine);
        if (matcher.find()) {
            return Optional.of(new SimpleErrorTrace(
                    Integer.parseInt(matcher.group(1)),
                    matcher.group(7),
                    matcher.group(2),
                    new Range<>(
                            new SourceLocation(
                                    Integer.parseInt(matcher.group(3)) - 1,
                                    Integer.parseInt(matcher.group(4)) - 1),
                            new SourceLocation(
                                    Integer.parseInt(matcher.group(5)) - 1,
                                    Integer.parseInt(matcher.group(6)))),
                    variables));
        }
        matcher = SPECIAL_ERROR_TRACE_PATTERN.matcher(firstLine);
        if (matcher.find()) {
            return Optional.of(new SpecialErrorTrace(
                    Integer.parseInt(matcher.group(1)),
                    matcher.group(2).trim(),
                    variables));
        }
        matcher = BACK_TO_STATE_PATTERN.matcher(firstLine);
        if (matcher.find()) {
            return Optional.of(new BackToStateErrorTrace(
                    Integer.parseInt(matcher.group(1)),
                    matcher.group(7),
                    matcher.group(2),
                    new Range<>(
                            new SourceLocation(
                                    Integer.parseInt(matcher.group(3)) - 1,
                                    Integer.parseInt(matcher.group(4)) - 1),
                            new SourceLocation(
                                    Integer.parseInt(matcher.group(5)) - 1,
                                    Integer.parseInt(matcher.group(6)))),
                    variables));
        }

        return Optional.empty();
    }

    private List<TraceVariable> parseVariables(String text) {
        PsiParser parser = PARSER_DEFINITION.createParser(null);
        Lexer lexer = PARSER_DEFINITION.createLexer(null);

        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(
                PARSER_DEFINITION, lexer, text);
        ASTNode node = parser.parse(TLC_ERROR_TRACE, builder);
        ASTNode traceNode = node.findChildByType(
                TokenSet.create(SINGLE_VARIABLE_TRACE, MULTIPLE_VARIABLE_TRACE));
        if (traceNode == null) {
            return emptyList();
        }
        return Arrays.stream(traceNode.getChildren(TokenSet.create(TLC_VARIABLE_DECL)))
                     .flatMap(decl -> {
                         Optional<String> nameOpt =
                                 Optional.ofNullable(decl.findChildByType(IDENTIFIER))
                                         .map(ASTNode::getText);
                         Optional<TraceVariableValue> valueOpt =
                                 Optional.ofNullable(decl.findChildByType(TLC_VALUE))
                                         .map(TLCErrorTraceParser::parseVariableValue);
                         return nameOpt.flatMap(
                                 name -> valueOpt.map(
                                         value -> new TraceVariable(name, value))).stream();
                     })
                     .collect(toList());
    }

    private static TraceVariableValue parseVariableValue(ASTNode valueNode) {
        ASTNode node = valueNode.getFirstChildNode();
        if (node == null) {
            return new UnknownValue(valueNode.getText());
        }
        IElementType type = node.getElementType();
        if (type == TLC_PRIMITIVE_VALUE) {
            return new PrimitiveValue(node.getText());
        }
        if (type == TLC_SEQUENCE_EMPTY_VALUE || type == TLC_SEQUENCE_VALUE) {
            List<TraceVariableValue> values =
                    Arrays.stream(node.getChildren(TokenSet.create(TLC_VALUE)))
                          .map(TLCErrorTraceParser::parseVariableValue)
                          .collect(toList());
            return new SequenceValue(values);
        }
        if (type == TLC_SET_EMPTY_VALUE || type == TLC_SET_VALUE) {
            List<TraceVariableValue> values =
                    Arrays.stream(node.getChildren(TokenSet.create(TLC_VALUE)))
                          .map(TLCErrorTraceParser::parseVariableValue)
                          .collect(toList());
            return new SetValue(values);
        }
        if (type == TLC_RECORD_VALUE) {
            List<RecordValue.Entry> entries =
                    Arrays.stream(node.getChildren(TokenSet.create(TLC_RECORD_PAIR)))
                          .flatMap(pairNode -> {
                              Optional<String> keyOpt =
                                      Optional.ofNullable(pairNode.findChildByType(IDENTIFIER))
                                              .map(ASTNode::getText);
                              Optional<TraceVariableValue> valueOpt =
                                      Optional.ofNullable(pairNode.findChildByType(TLC_VALUE))
                                              .map(TLCErrorTraceParser::parseVariableValue);
                              return keyOpt.flatMap(key -> valueOpt.map(value -> new RecordValue.Entry(key, value)))
                                           .stream();
                          })
                          .collect(toList());
            return new RecordValue(entries);
        }
        if (type == TLC_FUNCTION_VALUE) {
            List<FunctionValue.Entry> entries =
                    Arrays.stream(node.getChildren(TokenSet.create(TLC_FUNCTION_PAIR)))
                          .flatMap(pairNode -> {
                              Optional<String> keyOpt =
                                      Optional.ofNullable(pairNode.findChildByType(IDENTIFIER))
                                              .map(ASTNode::getText);
                              Optional<TraceVariableValue> valueOpt =
                                      Optional.ofNullable(pairNode.findChildByType(TLC_VALUE))
                                              .map(TLCErrorTraceParser::parseVariableValue);
                              return keyOpt.flatMap(key -> valueOpt.map(value -> new FunctionValue.Entry(key, value)))
                                           .stream();
                          })
                          .collect(toList());
            return new FunctionValue(entries);
        }
        return new UnknownValue(valueNode.getText());
    }
}
