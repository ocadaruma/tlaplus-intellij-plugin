package com.mayreh.intellij.plugin.tlaplus.psi.ext;

import static com.mayreh.intellij.plugin.tlaplus.psi.TLAplusPsiUtils.isForwardReference;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusFuncDefinition;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusLetExpr;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusNamedElement;

public abstract class TLAplusLetExprImplMixin extends TLAplusElementImpl implements TLAplusLetExpr {
    protected TLAplusLetExprImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Stream<TLAplusNamedElement> localDefinitions(
            @NotNull TLAplusElement placement) {
        Stream.Builder<Stream<TLAplusNamedElement>> streams = Stream.builder();

        streams.add(
                getOpDefinitionList()
                        .stream()
                        .flatMap(def -> Stream.of(def.getNonfixLhs(),
                                                  def.getPrefixOpLhs(),
                                                  def.getDashdotOpLhs(),
                                                  def.getInfixOpLhs(),
                                                  def.getPostfixOpLhs())
                                              .filter(Objects::nonNull)
                                              .findFirst()
                                              .map(TLAplusNamedLhs::getNamedElement)
                                              .filter(namedElement -> !isForwardReference(placement, namedElement))
                                              .stream()));

        streams.add(getFuncDefinitionList()
                            .stream()
                            .map(TLAplusFuncDefinition::getFuncName)
                            .filter(e -> !isForwardReference(placement, e))
                            .map(Function.identity()));

        streams.add(getModuleDefinitionList()
                            .stream()
                            .map(e -> e.getNonfixLhs().getNonfixLhsName())
                            .filter(name -> !isForwardReference(placement, name))
                            .map(Function.identity()));

        return streams.build().flatMap(Function.identity());
    }
}
