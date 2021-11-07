package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jetbrains.annotations.Nullable;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariable;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;

import lombok.Getter;
import lombok.experimental.Accessors;

class ErrorTraceTreeTable extends TreeTable {
    private final ErrorTraceModel treeTableModel;

    static class ErrorTraceModel extends ListTreeTableModel {
        private final DefaultMutableTreeNode rootNode;
        ErrorTraceModel(DefaultMutableTreeNode rootNode, ColumnInfo[] columns) {
            super(rootNode, columns);
            this.rootNode = rootNode;
        }

        ErrorTraceModel() {
            this(new DefaultMutableTreeNode("root"), new ColumnInfo[]{
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
                                    setValue("");
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

    @Getter
    @Accessors(fluent = true)
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
    }

    void addState(StateRootNode stateRoot, List<TraceVariable> variables) {

    }
}
