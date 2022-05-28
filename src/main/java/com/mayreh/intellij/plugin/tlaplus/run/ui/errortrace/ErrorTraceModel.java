package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jetbrains.annotations.Nullable;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;

class ErrorTraceModel extends ListTreeTableModel {
    public static final int NAME_COLUMN_INDEX = 0;
    public static final int VALUE_COLUMN_INDEX = 1;


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
