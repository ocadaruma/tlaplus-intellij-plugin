package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.BackToStateErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SimpleErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SpecialErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;
import com.mayreh.intellij.plugin.tlaplus.run.ui.ActionFormula;

import lombok.Getter;
import lombok.experimental.Accessors;

class StateRootNode extends DefaultMutableTreeNode {
    @Getter
    @Accessors(fluent = true)
    private final List<TextFragment> textRepresentation;
    private final HashMap<String, TraceVariableValue> variables = new HashMap<>();

    StateRootNode(ErrorTraceEvent errorTraceEvent) {
        this(textRepresentation(errorTraceEvent));
    }

    private StateRootNode(List<TextFragment> textRepresentation) {
        // We call super to set DefaultMutableTreeNode's userObject to make
        // copy&paste works
        super(textRepresentation.stream().map(TextFragment::text).collect(Collectors.joining()));
        this.textRepresentation = textRepresentation;
    }

    private static List<TextFragment> textRepresentation(ErrorTraceEvent errorTraceEvent) {
        List<TextFragment> fragments = new ArrayList<>();

        // NOTE: We should not use character < or > for cell value because
        // default JTree's transfer handler (javax.swing.plaf.basic.BasicTreeUI.TreeTransferHandler) embeds it inside
        // <html> tag, which could cause corrupted html
        if (errorTraceEvent instanceof SpecialErrorTrace) {
            SpecialErrorTrace trace = (SpecialErrorTrace) errorTraceEvent;
            fragments.add(new TextFragment(String.format("%d. %s", trace.number(), trace.type()), null));
        }
        if (errorTraceEvent instanceof SimpleErrorTrace) {
            SimpleErrorTrace trace = (SimpleErrorTrace) errorTraceEvent;
            fragments.add(new TextFragment(trace.number() + ". ", null));
            fragments.add(new TextFragment(
                    String.format("%s:%s", trace.module(), trace.action()),
                    new ActionFormula(trace.module(), trace.action(), trace.range().getFrom())));
        }
        if (errorTraceEvent instanceof BackToStateErrorTrace) {
            BackToStateErrorTrace trace = (BackToStateErrorTrace) errorTraceEvent;
            fragments.add(new TextFragment(trace.number() + ". Back to state ", null));
            fragments.add(new TextFragment(
                    String.format("%s:%s", trace.module(), trace.action()),
                    new ActionFormula(trace.module(), trace.action(), trace.range().getFrom())));
        }
        return fragments;
    }

    void addVariableNode(TraceVariableNode node) {
        variables.put(node.key, node.value.value());
        add(node);
    }

    Optional<TraceVariableValue> lookupVariable(String name) {
        return Optional.ofNullable(variables.get(name));
    }
}
