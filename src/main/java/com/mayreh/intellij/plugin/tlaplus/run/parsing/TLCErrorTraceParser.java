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
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.intellij.core.CoreApplicationEnvironment;
import com.intellij.core.CoreProjectEnvironment;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.lang.impl.PsiBuilderImpl;
import com.intellij.lang.impl.TokenSequence;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.Disposable;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusElementTypes;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.FunctionValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.PrimitiveValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.RecordValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.SequenceValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.SetValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.TraceVariable;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.TraceVariableValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTrace.UnknownValue;

public class TLCErrorTraceParser {
    private static final TLCErrorTraceParserDefinition PARSER_DEFINITION = new TLCErrorTraceParserDefinition();

    // The technique parsing by using CoreProjectEnvironment is taken from
    // https://github.com/JetBrains/Grammar-Kit/blob/2021.1.2/src/org/intellij/grammar/LightPsi.java#L165
    private static final CoreProjectEnvironment DUMMY_PROJECT_ENV;
    static {
        Disposable noopDisposable = () -> {};
        DUMMY_PROJECT_ENV = new CoreProjectEnvironment(
                noopDisposable, new CoreApplicationEnvironment(noopDisposable));
    }

    public static Optional<TLCEvent.ErrorTrace> parse(String text) {
        PsiParser parser = PARSER_DEFINITION.createParser(null);
        Lexer lexer = PARSER_DEFINITION.createLexer(null);
        PsiBuilderImpl builder = new PsiBuilderImpl(
                DUMMY_PROJECT_ENV.getProject(),
                null,
                PARSER_DEFINITION,
                lexer,
                null,
                text,
                null,
                null);
        ASTNode node = parser.parse(TLC_ERROR_TRACE, builder);
        ASTNode traceNode = node.findChildByType(
                TokenSet.create(SINGLE_VARIABLE_TRACE, MULTIPLE_VARIABLE_TRACE));
        if (traceNode == null) {
            return Optional.empty();
        }
        List<TraceVariable> variables =
                Arrays.stream(traceNode.getChildren(TokenSet.create(TLC_VARIABLE_DECL)))
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
        return Optional.of(new ErrorTrace(variables));
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
