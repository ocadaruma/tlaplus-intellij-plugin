package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TextIcon;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.table.IconTableCellRenderer;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.BackToStateErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.FunctionValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.RecordValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SequenceValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SetValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SimpleErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SpecialErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariable;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;
import com.mayreh.intellij.plugin.tlaplus.run.ui.ErrorTraceTreeTable.ErrorTraceCellRenderer.TextFragment;
import com.mayreh.intellij.plugin.util.Optionalx;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@SuppressWarnings("rawtypes")
class ErrorTraceTreeTable extends TreeTable {
    private static final int VALUE_COLUMN_INDEX = 1;

    private final ErrorTraceModel treeTableModel;

    static class ErrorTraceModel extends ListTreeTableModel {
        private final DefaultMutableTreeNode rootNode;
        private final List<StateRootNode> stateRootNodes = new ArrayList<>();

        ErrorTraceModel(DefaultMutableTreeNode rootNode, ColumnInfo[] columns) {
            super(rootNode, columns);
            this.rootNode = rootNode;
        }

        ErrorTraceModel() {
            this(new DefaultMutableTreeNode("ROOT"), new ColumnInfo[]{
                    new TreeColumnInfo("Name"),
                    new ColumnInfo("Value") {
                        @Nullable
                        @Override
                        public Object valueOf(Object o) {
                            if (o instanceof TraceVariableNode) {
                                return ((TraceVariableNode) o).value;
                            }
                            return null;
                        }
                    }
            });
        }

        void addStateRoot(StateRootNode stateRootNode) {
            rootNode.add(stateRootNode);
            stateRootNodes.add(stateRootNode);
        }

        void nodeStructureChanged() {
            nodeStructureChanged(rootNode);
        }

        Optional<StateRootNode> lastState() {
            if (stateRootNodes.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(stateRootNodes.get(stateRootNodes.size() - 1));
        }
    }

    static class StateRootNode extends DefaultMutableTreeNode {
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
            variables.put(node.key, node.value.value);
            add(node);
        }

        Optional<TraceVariableValue> lookupVariable(String name) {
            return Optional.ofNullable(variables.get(name));
        }
    }

    @Value
    @Accessors(fluent = true)
    static class TraceVariableValueWrapper {
        ValueDiffType diffType;
        TraceVariableValue value;

        // Should return TraceVariableValue#asString so that it will be rendered as
        // cell text and clipboard content
        @Override
        public String toString() {
            return value.asString();
        }
    }

    enum ValueDiffType {
        Unmodified,
        Modified,
        Added,
    }

    static class TraceVariableNode extends DefaultMutableTreeNode {
        public final String key;
        public final TraceVariableValueWrapper value;

        TraceVariableNode(String key, TraceVariableValue value, ValueDiffType diffType) {
            super(key);
            this.key = key;
            this.value = new TraceVariableValueWrapper(diffType, value);
        }
    }

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
        addMouseMotionListener(mouseListener);

        IconTableCellRenderer renderer = new IconTableCellRenderer<TraceVariableValueWrapper>() {
            @Override
            protected Icon getIcon(@NotNull TraceVariableValueWrapper value, JTable table, int row) {
                final String text;
                final Color foreground;
                switch (value.diffType) {
                    case Modified:
                        text = "M";
                        // We prefer UNKNOWN rather than FileStatus.MODIFIED for better visibility
                        foreground = FileStatus.UNKNOWN.getColor();
                        break;
                    case Added:
                        text = "A";
                        foreground = FileStatus.ADDED.getColor();
                        break;
                    default:
                        // We fill dummy text even when unchanged to allocate same size of rectangle
                        text = "_";
                        foreground = UIUtil.TRANSPARENT_COLOR;
                        break;
                }
                TextIcon icon = new TextIcon(text, foreground, null, 1);
                icon.setFont(RelativeFont.NORMAL.family(Font.MONOSPACED).derive(getFont()));
                return icon;
            }
        };
        getColumnModel().getColumn(VALUE_COLUMN_INDEX).setCellRenderer(renderer);
    }

    ErrorTraceTreeTable(@Nullable TLAplusModule module) {
        this(new ErrorTraceModel(), module);
    }

    /**
     * Add a state consists of variable assignments to the error-trace tree.
     */
    void addState(StateRootNode stateRoot, List<TraceVariable> variables) {
        for (TraceVariable variable : variables) {
            ValueDiffType diffType = treeTableModel
                    .lastState()
                    .map(lastState -> lastState.lookupVariable(variable.name()).map(
                            lastValue -> lastValue.equals(variable.value()) ?
                                         ValueDiffType.Unmodified : ValueDiffType.Modified).orElse(ValueDiffType.Added))
                    .orElse(ValueDiffType.Unmodified);

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

    void expandStates() {
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
            Optional<SequenceValue> lastSequence = lastValue.flatMap(v -> Optionalx.asInstanceOf(v, SequenceValue.class));
            SequenceValue sequence = (SequenceValue) value;
            for (int i = 0; i < sequence.values().size(); i++) {
                TraceVariableValue subValue = sequence.values().get(i);

                final ValueDiffType diffType;
                final Optional<TraceVariableValue> maybeLastSubValue;
                if (lastSequence.isPresent()) {
                    List<TraceVariableValue> lastValues = lastSequence.get().values();
                    if (i < lastValues.size()) {
                        TraceVariableValue lastSubValue = lastValues.get(i);
                        diffType = subValue.equals(lastSubValue) ? ValueDiffType.Unmodified : ValueDiffType.Modified;
                        maybeLastSubValue = Optional.of(lastSubValue);
                    } else {
                        diffType = ValueDiffType.Added;
                        maybeLastSubValue = Optional.empty();
                    }
                } else {
                    diffType = ValueDiffType.Unmodified;
                    maybeLastSubValue = Optional.empty();
                }

                TraceVariableNode node = new TraceVariableNode("[" + (i + 1) + "]", subValue, diffType);
                parent.add(node);
                renderTraceVariableValue(node, subValue, maybeLastSubValue);
            }
        }
        if (value instanceof SetValue) {
            Optional<SetValue> lastSet = lastValue.flatMap(v -> Optionalx.asInstanceOf(v, SetValue.class));
            for (TraceVariableValue subValue : ((SetValue) value).values()) {
                final ValueDiffType diffType;
                final Optional<TraceVariableValue> maybeLastSubValue;
                if (lastSet.isPresent()) {
                    if (lastSet.get().values().contains(subValue)) {
                        diffType = ValueDiffType.Unmodified;
                        maybeLastSubValue = Optional.of(subValue);
                    } else {
                        diffType = ValueDiffType.Added;
                        maybeLastSubValue = Optional.empty();
                    }
                } else {
                    diffType = ValueDiffType.Unmodified;
                    maybeLastSubValue = Optional.empty();
                }

                TraceVariableNode node = new TraceVariableNode("*", subValue, diffType);
                parent.add(node);
                renderTraceVariableValue(node, subValue, maybeLastSubValue);
            }
        }
        if (value instanceof RecordValue) {
            Optional<RecordValue> lastRecord = lastValue.flatMap(v -> Optionalx.asInstanceOf(v, RecordValue.class));
            for (RecordValue.Entry entry : ((RecordValue) value).entries()) {
                final ValueDiffType diffType;
                final Optional<TraceVariableValue> maybeLastSubValue;

                if (lastRecord.isPresent()) {
                    maybeLastSubValue = lastRecord.flatMap(record -> record.lookup(entry.key()));
                    if (maybeLastSubValue.isPresent()) {
                        diffType = entry.value().equals(maybeLastSubValue.get()) ?
                                   ValueDiffType.Unmodified : ValueDiffType.Modified;
                    } else {
                        diffType = ValueDiffType.Added;
                    }
                } else {
                    diffType = ValueDiffType.Unmodified;
                    maybeLastSubValue = Optional.empty();
                }

                TraceVariableNode node = new TraceVariableNode(entry.key(), entry.value(), diffType);
                parent.add(node);
                renderTraceVariableValue(node, entry.value(), maybeLastSubValue);
            }
        }
        if (value instanceof FunctionValue) {
            Optional<FunctionValue> lastFunction = lastValue.flatMap(v -> Optionalx.asInstanceOf(v, FunctionValue.class));
            for (FunctionValue.Entry entry : ((FunctionValue) value).entries()) {
                final ValueDiffType diffType;
                final Optional<TraceVariableValue> maybeLastSubValue;

                if (lastFunction.isPresent()) {
                    maybeLastSubValue = lastFunction.flatMap(func -> func.lookup(entry.key()));
                    if (maybeLastSubValue.isPresent()) {
                        diffType = entry.value().equals(maybeLastSubValue.get()) ?
                                   ValueDiffType.Unmodified : ValueDiffType.Modified;
                    } else {
                        diffType = ValueDiffType.Added;
                    }
                } else {
                    diffType = ValueDiffType.Unmodified;
                    maybeLastSubValue = Optional.empty();
                }

                TraceVariableNode node = new TraceVariableNode(entry.key(), entry.value(), diffType);
                parent.add(node);
                renderTraceVariableValue(node, entry.value(), maybeLastSubValue);
            }
        }
    }

    @RequiredArgsConstructor
    static class ErrorTraceCellRenderer extends ColoredTreeCellRenderer {
        private final TreeTable treeTable;

        @Override
        public void customizeCellRenderer(
                @NotNull JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            if (!(value instanceof StateRootNode)) {
                append(value == null ? "" : value.toString());
                return;
            }
            List<TextFragment> fragments = ((StateRootNode) value).textRepresentation;
            for (TextFragment fragment : fragments) {
                final Color unselectedForeground;
                final int attributes;
                if (fragment.actionFormula != null) {
                    unselectedForeground = JBUI.CurrentTheme.Link.Foreground.ENABLED;
                    attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES.getStyle() | SimpleTextAttributes.STYLE_UNDERLINE;
                } else {
                    unselectedForeground = treeTable.getForeground();
                    attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES.getStyle();
                }

                final Color foreground;
                if (selected) {
                    foreground = treeTable.getSelectionForeground();
                } else {
                    foreground = unselectedForeground;
                }

                append(fragment.text, new SimpleTextAttributes(attributes, foreground), fragment.actionFormula);
            }

            // common
            setFont(tree.getFont());
            setBorder(Borders.empty());
        }

        @Value
        @Accessors(fluent = true)
        static class TextFragment {
            String text;
            @Nullable ActionFormula actionFormula;
        }
    }
}
