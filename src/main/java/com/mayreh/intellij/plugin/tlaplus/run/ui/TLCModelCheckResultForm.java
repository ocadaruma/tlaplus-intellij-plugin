package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBFont;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageInit;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageItem;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageNext;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ProcessTerminated;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.Progress;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCError;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCFinished;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCStart;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCSuccess;

public class TLCModelCheckResultForm {
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    private JPanel panel;
    private JLabel statusLabel;
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

    // We want to set statusLabel from exitCode=0 event only when
    // TLCFinished event is not received yet. (though not sure if such case can happen)
    private boolean statusSet = false;

    public JComponent component() {
        return panel;
    }

    private void createUIComponents() {
        errorTraceTree = new Tree();
        errorTraceTree.setRootVisible(false);
        DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();
        cellRenderer.setOpenIcon(null);
        cellRenderer.setClosedIcon(null);
        cellRenderer.setLeafIcon(null);
        errorTraceTree.setCellRenderer(cellRenderer);
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

//        DefaultMutableTreeNode abcde = new DefaultMutableTreeNode("1: abcde");
//        DefaultMutableTreeNode ghijk = new DefaultMutableTreeNode("2: ghijk");
//        DefaultMutableTreeNode lmnop = new DefaultMutableTreeNode("1-1: lmnop");
//
//        errorTraceModel.insertNodeInto(lmnop, abcde, abcde.getChildCount());
//        errorTraceModel.insertNodeInto(abcde, errorTraceRoot, errorTraceRoot.getChildCount());
//        errorTraceModel.insertNodeInto(ghijk, errorTraceRoot, errorTraceRoot.getChildCount());
//        errorTraceModel.nodeStructureChanged(errorTraceRoot);
    }

    public void onEvent(TLCEvent event) {
        if (event instanceof TLCEvent.SANYStart) {
            statusLabel.setText("SANY running");
        }
        if (event instanceof TLCEvent.SANYEnd) {
            statusLabel.setText("SANY finished");
        }
        if (event instanceof TLCEvent.TLCStart) {
            statusLabel.setIcon(AnimatedIcon.Default.INSTANCE);
            statusLabel.setText("TLC running");
            startLabel.setText(((TLCStart) event).startedAt().format(DATETIME_FORMAT));
        }
        if (event instanceof Progress) {
            Progress progress = (Progress) event;
            statesTableModel.addRow(Arrays.asList(
                    progress.timestamp().format(DATETIME_FORMAT),
                    String.valueOf(progress.diameter()),
                    String.valueOf(progress.total()),
                    String.valueOf(progress.distinct()),
                    String.valueOf(progress.queueSize())
            ));
        }
        if (event instanceof CoverageInit || event instanceof CoverageNext) {
            if (event instanceof CoverageInit) {
                coverageTableModel.clearRows();
            }
            CoverageItem coverage = event instanceof CoverageInit ?
                                    ((CoverageInit) event).item() : ((CoverageNext) event).item();
            coverageTableModel.addRow(Arrays.asList(
                    coverage.module(),
                    coverage.action(),
                    String.valueOf(coverage.total()),
                    String.valueOf(coverage.distinct())
            ));
        }
        if (event instanceof TLCFinished) {
            statusLabel.setIcon(null);
            if (!statusSet) {
                statusLabel.setText("TLC finished");
            }
            endLabel.setText(((TLCFinished) event).finishedAt().format(DATETIME_FORMAT));
        }
        if (event instanceof TLCSuccess) {
            statusLabel.setIcon(null);
            statusLabel.setText(String.format("Succeeded (Fingerprint collision probability: %f)",
                                              ((TLCSuccess) event).fingerprintCollisionProbability()));
            statusLabel.setFont(JBFont.label().asBold());
            statusLabel.setForeground(ColorProgressBar.GREEN);
            statusSet = true;
        }
        if (event instanceof TLCError) {
            errorsTableModel.addRow(Collections.singletonList(((TLCError) event).message()));
        }
        if (event instanceof ProcessTerminated) {
            statusLabel.setIcon(null);
            statusLabel.setFont(JBFont.label().asBold());
            int exitCode = ((ProcessTerminated) event).exitCode();
            // SIGKILL (forcibly stopped by button)
            if (exitCode == 137) {
                statusLabel.setForeground(ColorProgressBar.YELLOW);
                statusLabel.setText("Aborted");
            } else if (exitCode != 0) {
                statusLabel.setForeground(ColorProgressBar.RED_TEXT);
                statusLabel.setText("Failed");
            } else if (!statusSet) {
                statusLabel.setForeground(ColorProgressBar.GREEN);
                statusLabel.setText("Finished");
            }
        }
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

        public void clearRows() {
            while (getRowCount() > 0) {
                removeRow(0);
            }
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
