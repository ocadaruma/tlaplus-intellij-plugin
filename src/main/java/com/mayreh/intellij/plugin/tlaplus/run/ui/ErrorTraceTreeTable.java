package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jetbrains.annotations.Nullable;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.FunctionValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.RecordValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SequenceValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SetValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariable;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;

@SuppressWarnings({"rawtypes", "unchecked"})
class ErrorTraceTreeTable extends TreeTable {
    private final ErrorTraceModel treeTableModel;

    static class ErrorTraceModel extends ListTreeTableModel {
        private final DefaultMutableTreeNode rootNode;
        private final ColumnInfo[] columns;

        ErrorTraceModel(DefaultMutableTreeNode rootNode, ColumnInfo[] columns) {
            super(rootNode, columns);
            this.rootNode = rootNode;
            this.columns = columns;
        }

        ErrorTraceModel() {
            this(new DefaultMutableTreeNode("ROOT"), new ColumnInfo[]{
                    new TreeColumnInfo("Name"),
                    new TreeColumnInfo("Value") {
                        @Override
                        public @Nullable TableCellRenderer getRenderer(Object o) {
                            return new DefaultTableCellRenderer() {
                                @Override
                                public Component getTableCellRendererComponent(
                                        JTable table,
                                        Object value,
                                        boolean isSelected,
                                        boolean hasFocus,
                                        int row,
                                        int column) {
                                    Component component = super.getTableCellRendererComponent(
                                            table, value, isSelected, hasFocus, row, column);
                                    if (o instanceof TraceVariableNode) {
                                        setValue(((TraceVariableNode) o).value.asString());
                                    } else {
                                        setValue("");
                                    }
                                    return component;
                                }
                            };
                        }
                    }
            });
        }
    }

    static class StateRootNode extends DefaultMutableTreeNode {
        StateRootNode(String value) {
            super(value);
        }
    }

    static class TraceVariableNode extends DefaultMutableTreeNode {
        private final String key;
        private final TraceVariableValue value;

        TraceVariableNode(String key, TraceVariableValue value) {
            super(key);

            this.key = key;
            this.value = value;
        }
    }

    ErrorTraceTreeTable(ErrorTraceModel treeTableModel) {
        super(treeTableModel);
        this.treeTableModel = treeTableModel;
        setRootVisible(false);
    }

    ErrorTraceTreeTable() {
        this(new ErrorTraceModel());
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        TreePath treePath = getTree().getPathForRow(row);
        if (treePath == null) {
            return super.getCellRenderer(row, column);
        }
        Object node = treePath.getLastPathComponent();
        TableCellRenderer renderer = treeTableModel.columns[column].getRenderer(node);
        return renderer == null ? super.getCellRenderer(row, column) : renderer;
    }

    /**
     * Add a state consists of variable assignments to the error-trace tree.
     */
    void addState(StateRootNode stateRoot, List<TraceVariable> variables) {
        treeTableModel.rootNode.add(stateRoot);
        for (TraceVariable variable : variables) {
            TraceVariableNode variableNode = new TraceVariableNode(variable.name(), variable.value());
            stateRoot.add(variableNode);
            renderTraceVariableValue(variableNode, variable.value());
        }
        treeTableModel.nodeStructureChanged(treeTableModel.rootNode);
    }

    private static void renderTraceVariableValue(
            DefaultMutableTreeNode parent,
            TraceVariableValue value) {
        if (!value.hasChildren()) {
            return;
        }
        if (value instanceof SequenceValue) {
            SequenceValue sequence = (SequenceValue) value;
            for (int i = 0; i < sequence.values().size(); i++) {
                TraceVariableValue subValue = sequence.values().get(i);
                TraceVariableNode node = new TraceVariableNode("[" + (i + 1) + "]", subValue);
                parent.add(node);
                renderTraceVariableValue(node, subValue);
            }
        }
        if (value instanceof SetValue) {
            for (TraceVariableValue subValue : ((SetValue) value).values()) {
                TraceVariableNode node = new TraceVariableNode("*", subValue);
                parent.add(node);
                renderTraceVariableValue(node, subValue);
            }
        }
        if (value instanceof RecordValue) {
            for (RecordValue.Entry entry : ((RecordValue) value).entries()) {
                TraceVariableNode node = new TraceVariableNode(entry.key(), entry.value());
                parent.add(node);
                renderTraceVariableValue(node, entry.value());
            }
        }
        if (value instanceof FunctionValue) {
            for (FunctionValue.Entry entry : ((FunctionValue) value).entries()) {
                TraceVariableNode node = new TraceVariableNode(entry.key(), entry.value());
                parent.add(node);
                renderTraceVariableValue(node, entry.value());
            }
        }
    }
}
