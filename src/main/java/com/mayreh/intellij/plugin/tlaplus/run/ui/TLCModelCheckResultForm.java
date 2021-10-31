package com.mayreh.intellij.plugin.tlaplus.run.ui;

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

import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI.Borders;

public class TLCModelCheckResultForm {
    private JPanel panel;
    private JLabel resultLabel;
    private JLabel startLabel;
    private JLabel endLabel;
    private JTable statesTable;
    private JTable coverageTable;
    private JList errorsList;
    private JTree errorTraceTree;
    private JScrollPane statesTableScrollPane;
    private JScrollPane coverageTableScrollPane;
    private TableModel statesTableModel;

    public JComponent component() {
        return panel;
    }

    private void createUIComponents() {
        statesTableModel = new TableModel("Time", "Diameter", "Found", "Distinct", "Queue");
        statesTable = new JBTable(statesTableModel) {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(getPreferredSize().width, getRowHeight() * getRowCount());
            }
        };
        coverageTable = new JBTable(new TableModel("Module", "Action", "Total", "Distinct"));
        statesTable.setCellSelectionEnabled(false);
        coverageTable.setCellSelectionEnabled(false);
    }

    public void initUI() {
        statesTableScrollPane.setWheelScrollingEnabled(false);
        coverageTableScrollPane.setWheelScrollingEnabled(false);
        clearMouseWheelListeners(statesTableScrollPane);
        clearMouseWheelListeners(coverageTableScrollPane);
        clearMouseWheelListeners(statesTable);
        clearMouseWheelListeners(coverageTable);
    }

    public void notify(String message) {
        statesTableModel.addRow(Arrays.asList(
                String.valueOf(ThreadLocalRandom.current().nextInt()), "2", "3", "4", "5"));
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

    private static void clearMouseWheelListeners(JComponent component) {
        for (MouseWheelListener listener : component.getMouseWheelListeners()) {
            component.removeMouseWheelListener(listener);
        }
    }
}
