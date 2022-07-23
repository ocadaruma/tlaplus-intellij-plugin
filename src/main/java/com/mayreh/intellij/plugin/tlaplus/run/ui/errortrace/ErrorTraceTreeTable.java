package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import static com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace.ErrorTraceModel.NAME_COLUMN_INDEX;
import static com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace.ErrorTraceModel.VALUE_COLUMN_INDEX;
import static com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace.ValueDiffType.Added;
import static com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace.ValueDiffType.Modified;
import static com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace.ValueDiffType.Unmodified;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jetbrains.annotations.Nullable;

import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableCellRenderer;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.FunctionValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.RecordValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SequenceValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SetValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariable;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;
import com.mayreh.intellij.plugin.tlaplus.run.ui.ActionFormula;
import com.mayreh.intellij.plugin.tlaplus.run.ui.ActionFormulaLinkListener;
import com.mayreh.intellij.plugin.util.Optionalx;

public class ErrorTraceTreeTable extends TreeTable {
    private final ErrorTraceModel treeTableModel;

    ErrorTraceTreeTable(ErrorTraceModel treeTableModel,
                        @Nullable TLAplusModule module) {
        super(treeTableModel);

        this.treeTableModel = treeTableModel;
        setRootVisible(false);
        setTreeCellRenderer(new ErrorTraceCellRenderer(this));
        ActionFormulaLinkListener mouseListener = new ActionFormulaLinkListener(module) {
            // Taken from com.intellij.execution.services.ServiceViewTreeLinkMouseListener
            @Override
            protected @Nullable ActionFormula taggedActionFormula(MouseEvent e) {
                TreePath path = getTree().getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return null;
                }
                Rectangle rect = getTree().getPathBounds(path);
                if (rect == null) {
                    return null;
                }

                Object treeNode = path.getLastPathComponent();
                int row = getTree().getRowForLocation(e.getX(), e.getY());
                boolean isLeaf = getTree().getModel().isLeaf(treeNode);
                Component component = getTree().getCellRenderer().getTreeCellRendererComponent(
                        getTree(), treeNode, false, false, isLeaf, row, false);
                if (component instanceof SimpleColoredComponent) {
                    Object tag = ((SimpleColoredComponent) component).getFragmentTagAt(e.getX() - rect.x);
                    if (tag instanceof ActionFormula) {
                        return (ActionFormula) tag;
                    }
                }
                return null;
            }
        };
        addMouseListener(mouseListener);
        addMouseListener(new ErrorTraceMouseListener());
        addMouseMotionListener(mouseListener);
        getColumnModel().getColumn(NAME_COLUMN_INDEX).setCellRenderer(
                new StateSeparatingCellRenderer(new TreeTableCellRenderer(this, getTree())));
        getColumnModel().getColumn(VALUE_COLUMN_INDEX).setCellRenderer(
                new StateSeparatingCellRenderer(new ValueColumnCellRenderer()));
    }

    public ErrorTraceTreeTable(@Nullable TLAplusModule module) {
        this(new ErrorTraceModel(), module);
    }

    /**
     * Add a state consists of variable assignments to the error-trace tree.
     */
    public void addState(ErrorTraceEvent traceEvent, List<TraceVariable> variables) {
        StateRootNode stateRoot = new StateRootNode(traceEvent);
        for (TraceVariable variable : variables) {
            ValueDiffType diffType = treeTableModel
                    .lastState()
                    .map(lastState -> lastState.lookupVariable(variable.name()).map(
                            lastValue -> lastValue.equals(variable.value()) ? Unmodified : Modified).orElse(Added))
                    // lastState doesn't exist means this is initial state.
                    // for initial state, we assume all variables are unmodified.
                    .orElse(Unmodified);

            TraceVariableNode variableNode = new TraceVariableNode(variable.name(), variable.value(), diffType);
            stateRoot.addVariableNode(variableNode);
            renderTraceVariableValue(
                    variableNode,
                    variable.value(),
                    treeTableModel.lastState().flatMap(state -> state.lookupVariable(variable.name())));
        }
        treeTableModel.addStateRoot(stateRoot);
        treeTableModel.nodeStructureChanged();
    }

    public void expandStates() {
        // NOTE: getTree().getRowCount() may change during iteration due to expanding row
        for (int i = 0; i < getTree().getRowCount(); i++) {
            // Only expand direct child of the root (i.e. each state's roots) for better readability
            if (getTree().getPathForRow(i).getPathCount() == 2) {
                getTree().expandRow(i);
            }
        }
    }

    private static void renderTraceVariableValue(
            DefaultMutableTreeNode parent,
            TraceVariableValue value,
            Optional<TraceVariableValue> lastValue) {
        if (!value.hasChildren()) {
            return;
        }

        if (value instanceof SequenceValue) {
            renderSequenceValue(parent, (SequenceValue) value, lastValue);
        }
        if (value instanceof SetValue) {
            renderSetValue(parent, (SetValue) value, lastValue);
        }
        if (value instanceof RecordValue) {
            renderRecordValue(parent, (RecordValue) value, lastValue);
        }
        if (value instanceof FunctionValue) {
            renderFunctionValue(parent, (FunctionValue) value, lastValue);
        }
    }

    private static void renderSequenceValue(
            DefaultMutableTreeNode parent,
            SequenceValue sequence,
            Optional<TraceVariableValue> lastValue) {
        Optional<SequenceValue> lastSequence = lastValue.flatMap(v -> Optionalx.asInstanceOf(v, SequenceValue.class));
        for (int i = 0; i < sequence.values().size(); i++) {
            TraceVariableValue subValue = sequence.values().get(i);

            final ValueDiffType diffType;
            final Optional<TraceVariableValue> maybeLastSubValue;
            if (lastSequence.isPresent()) {
                List<TraceVariableValue> lastValues = lastSequence.get().values();
                if (i < lastValues.size()) {
                    TraceVariableValue lastSubValue = lastValues.get(i);
                    diffType = subValue.equals(lastSubValue) ? Unmodified : Modified;
                    maybeLastSubValue = Optional.of(lastSubValue);
                } else {
                    diffType = Added;
                    maybeLastSubValue = Optional.empty();
                }
            } else {
                diffType = Unmodified;
                maybeLastSubValue = Optional.empty();
            }

            TraceVariableNode node = new TraceVariableNode("[" + (i + 1) + "]", subValue, diffType);
            parent.add(node);
            renderTraceVariableValue(node, subValue, maybeLastSubValue);
        }
    }

    private static void renderSetValue(
            DefaultMutableTreeNode parent,
            SetValue set,
            Optional<TraceVariableValue> lastValue) {
        Optional<SetValue> lastSet = lastValue.flatMap(v -> Optionalx.asInstanceOf(v, SetValue.class));
        for (TraceVariableValue subValue : set.values()) {
            final ValueDiffType diffType;
            final Optional<TraceVariableValue> maybeLastSubValue;
            if (lastSet.isPresent()) {
                if (lastSet.get().values().contains(subValue)) {
                    diffType = Unmodified;
                    maybeLastSubValue = Optional.of(subValue);
                } else {
                    diffType = Added;
                    maybeLastSubValue = Optional.empty();
                }
            } else {
                diffType = Unmodified;
                maybeLastSubValue = Optional.empty();
            }

            TraceVariableNode node = new TraceVariableNode("*", subValue, diffType);
            parent.add(node);
            renderTraceVariableValue(node, subValue, maybeLastSubValue);
        }
    }

    private static void renderRecordValue(
            DefaultMutableTreeNode parent,
            RecordValue record,
            Optional<TraceVariableValue> lastValue) {
        Optional<RecordValue> lastRecord = lastValue.flatMap(v -> Optionalx.asInstanceOf(v, RecordValue.class));
        for (RecordValue.Entry entry : record.entries()) {
            final ValueDiffType diffType;
            final Optional<TraceVariableValue> maybeLastSubValue;

            if (lastRecord.isPresent()) {
                maybeLastSubValue = lastRecord.flatMap(r -> r.lookup(entry.key()));
                if (maybeLastSubValue.isPresent()) {
                    diffType = entry.value().equals(maybeLastSubValue.get()) ?
                               Unmodified : Modified;
                } else {
                    diffType = Added;
                }
            } else {
                diffType = Unmodified;
                maybeLastSubValue = Optional.empty();
            }

            TraceVariableNode node = new TraceVariableNode(entry.key(), entry.value(), diffType);
            parent.add(node);
            renderTraceVariableValue(node, entry.value(), maybeLastSubValue);
        }
    }

    private static void renderFunctionValue(
            DefaultMutableTreeNode parent,
            FunctionValue func,
            Optional<TraceVariableValue> lastValue) {
        Optional<FunctionValue> lastFunction = lastValue.flatMap(v -> Optionalx.asInstanceOf(v, FunctionValue.class));
        for (FunctionValue.Entry entry : func.entries()) {
            final ValueDiffType diffType;
            final Optional<TraceVariableValue> maybeLastSubValue;

            if (lastFunction.isPresent()) {
                maybeLastSubValue = lastFunction.flatMap(f -> f.lookup(entry.key()));
                if (maybeLastSubValue.isPresent()) {
                    diffType = entry.value().equals(maybeLastSubValue.get()) ? Unmodified : Modified;
                } else {
                    diffType = Added;
                }
            } else {
                diffType = Unmodified;
                maybeLastSubValue = Optional.empty();
            }

            TraceVariableNode node = new TraceVariableNode(entry.key(), entry.value(), diffType);
            parent.add(node);
            renderTraceVariableValue(node, entry.value(), maybeLastSubValue);
        }
    }
}
