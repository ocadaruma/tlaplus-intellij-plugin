package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI.Borders;

public class TLCModelCheckResultForm {
    private JPanel panel;
    private JLabel resultLabel;
    private JLabel startLabel;
    private JLabel endLabel;
    private JTree errorTraceTree;
    private JPanel statesTablePanel;
    private JPanel coverageTablePanel;
    private JPanel errorsPanel;
    private TableModel statesTableModel;
    private TableModel coverageTableModel;
    private TableModel errorsTableModel;

    private MutableTreeNode errorTraceRoot;
    private DefaultTreeModel errorTraceModel;

    public JComponent component() {
        return panel;
    }

    private void createUIComponents() {
    }

    public void initUI() {
        errorsTableModel = new TableModel("Error");
        JTable errorsTable = new SimpleTable(errorsTableModel);
        errorsPanel.add(errorsTable, BorderLayout.CENTER);

        statesTableModel = new TableModel("Time", "Diameter", "Found", "Distinct", "Queue");
        JTable statesTable = new SimpleTable(statesTableModel);
        statesTablePanel.add(statesTable.getTableHeader(), BorderLayout.NORTH);
        statesTablePanel.add(statesTable, BorderLayout.CENTER);

        coverageTableModel = new TableModel("Module", "Action", "Total", "Distinct");
        JTable coverageTable = new SimpleTable(coverageTableModel);
        coverageTablePanel.add(coverageTable.getTableHeader(), BorderLayout.NORTH);
        coverageTablePanel.add(coverageTable, BorderLayout.CENTER);

        errorTraceRoot = new DefaultMutableTreeNode("root");
        errorTraceModel = new DefaultTreeModel(errorTraceRoot);
        errorTraceTree.setModel(errorTraceModel);
    }

    public void notify(String message) {
        statesTableModel.addRow(Arrays.asList(
                String.valueOf(ThreadLocalRandom.current().nextInt()), "2", "3", "4", "5"));

        errorTraceModel.insertNodeInto(new DefaultMutableTreeNode(
                String.valueOf(ThreadLocalRandom.current().nextInt())
        ), errorTraceRoot, errorTraceRoot.getChildCount());
        errorTraceModel.nodeStructureChanged((TreeNode) errorTraceModel.getRoot());
    }

    public static class TableModel extends DefaultTableModel {
        public TableModel(String... headers) {
            super(new Vector<>(Arrays.asList(headers)), 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public void addRow(List<String> columns) {
            super.addRow(new Vector<>(columns));
        }
    }

    public static class SimpleTable extends JBTable {
        public SimpleTable(TableModel model) {
            super(model);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(getPreferredSize().width, getRowHeight() * getRowCount());
        }
    }
}