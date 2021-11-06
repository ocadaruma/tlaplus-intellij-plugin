package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.BackToStateErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.FunctionValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.PrimitiveValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.RecordValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SequenceValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SetValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SimpleErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SpecialErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariable;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.TraceVariableValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.UnknownValue;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ProcessTerminated;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.Progress;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.SANYEnd;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.SANYError;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCError;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCError.ErrorItem;
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
    private StatesTableModel statesTableModel;
    private CoverageTableModel coverageTableModel;
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
        errorsTable.setForeground(ColorProgressBar.RED_TEXT);
        errorsPanel.add(errorsTable, BorderLayout.CENTER);

        statesTableModel = new StatesTableModel("Time", "Diameter", "Found", "Distinct", "Queue");
        JTable statesTable = new SimpleTable(statesTableModel);
        statesTablePanel.add(statesTable.getTableHeader(), BorderLayout.NORTH);
        statesTablePanel.add(statesTable, BorderLayout.CENTER);

        coverageTableModel = new CoverageTableModel("Module", "Action", "Total", "Distinct");
        JTable coverageTable = new SimpleTable(coverageTableModel);
        coverageTablePanel.add(coverageTable.getTableHeader(), BorderLayout.NORTH);
        coverageTablePanel.add(coverageTable, BorderLayout.CENTER);

        errorTraceRoot = new DefaultMutableTreeNode("root");
        errorTraceModel = new DefaultTreeModel(errorTraceRoot);
        errorTraceTree.setModel(errorTraceModel);
    }

    public synchronized void onEvent(TLCEvent event) {
        if (event instanceof TLCEvent.SANYStart) {
            statusLabel.setText("SANY running");
        }
        if (event instanceof TLCEvent.SANYEnd) {
            statusLabel.setText("SANY finished");
            for (SANYError sanyError : ((SANYEnd) event).errors()) {
                String message = String.format(
                        "%s(%d:%d): %s",
                        sanyError.module(),
                        sanyError.location().line(),
                        sanyError.location().col(),
                        sanyError.message());
                errorsTableModel.addRow(Collections.singletonList(message));
            }
        }
        if (event instanceof TLCEvent.TLCStart) {
            statusLabel.setIcon(AnimatedIcon.Default.INSTANCE);
            statusLabel.setText("TLC running");
            startLabel.setText(((TLCStart) event).startedAt().format(DATETIME_FORMAT));
        }
        if (event instanceof Progress) {
            statesTableModel.addRow((Progress) event);
        }
        if (event instanceof CoverageInit || event instanceof CoverageNext) {
            if (event instanceof CoverageInit) {
                coverageTableModel.clearRows();
            }
            CoverageItem coverage = event instanceof CoverageInit ?
                                    ((CoverageInit) event).item() : ((CoverageNext) event).item();
            coverageTableModel.addRow(coverage);
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
            statusLabel.setText(String.format("Succeeded (Fingerprint collision probability: %E)",
                                              ((TLCSuccess) event).fingerprintCollisionProbability()));
            statusLabel.setFont(JBFont.label().asBold());
            statusLabel.setForeground(ColorProgressBar.GREEN);
            statusSet = true;
        }
        if (event instanceof TLCError) {
            for (ErrorItem error : ((TLCError) event).errors()) {
                errorsTableModel.addRow(Collections.singletonList(error.message()));
            }
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
        if (event instanceof SimpleErrorTrace) {
            SimpleErrorTrace trace = (SimpleErrorTrace) event;
            DefaultMutableTreeNode parent =
                    new DefaultMutableTreeNode(String.format(
                            "%d: <%s(%s):%d:%d>",
                            trace.number(),
                            trace.module(),
                            trace.action(),
                            trace.range().getFrom().line(),
                            trace.range().getFrom().col()));
            renderTraceVariables(parent, trace.variables());
        }
        if (event instanceof SpecialErrorTrace) {
            SpecialErrorTrace trace = (SpecialErrorTrace) event;
            DefaultMutableTreeNode parent =
                    new DefaultMutableTreeNode(String.format("%d: <%s>", trace.number(), trace.type()));
            renderTraceVariables(parent, trace.variables());
        }
        if (event instanceof BackToStateErrorTrace) {
            BackToStateErrorTrace trace = (BackToStateErrorTrace) event;
            DefaultMutableTreeNode parent =
                    new DefaultMutableTreeNode(String.format(
                            "%d: Back to state <%s(%s):%d:%d>",
                            trace.number(),
                            trace.module(),
                            trace.action(),
                            trace.range().getFrom().line(),
                            trace.range().getFrom().col()));
            renderTraceVariables(parent, trace.variables());
        }
    }

    private void renderTraceVariables(
            MutableTreeNode parent,
            List<TraceVariable> variables) {
        for (TraceVariable variable : variables) {
            DefaultMutableTreeNode nameNode = new DefaultMutableTreeNode(variable.name());
            errorTraceModel.insertNodeInto(nameNode, parent, parent.getChildCount());
            renderTraceVariableValue(nameNode, variable.value());
        }
        errorTraceModel.insertNodeInto(parent, errorTraceRoot, errorTraceRoot.getChildCount());
        errorTraceModel.nodeStructureChanged(errorTraceRoot);
    }

    private void renderTraceVariableValue(
            MutableTreeNode parent,
            TraceVariableValue value) {
        if (value instanceof PrimitiveValue) {
            DefaultMutableTreeNode valueNode =
                    new DefaultMutableTreeNode(((PrimitiveValue) value).content());
            errorTraceModel.insertNodeInto(valueNode, parent, parent.getChildCount());
        }
        if (value instanceof SequenceValue) {
            DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(":<<sequence>>");
            errorTraceModel.insertNodeInto(valueNode, parent, parent.getChildCount());
            for (TraceVariableValue subValue : ((SequenceValue) value).values()) {
                renderTraceVariableValue(valueNode, subValue);
            }
        }
        if (value instanceof SetValue) {
            DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(":{set}");
            errorTraceModel.insertNodeInto(valueNode, parent, parent.getChildCount());
            for (TraceVariableValue subValue : ((SetValue) value).values()) {
                renderTraceVariableValue(valueNode, subValue);
            }
        }
        if (value instanceof RecordValue) {
            DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(":[record]");
            errorTraceModel.insertNodeInto(valueNode, parent, parent.getChildCount());
            for (RecordValue.Entry entry : ((RecordValue) value).entries()) {
                DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(entry.key());
                errorTraceModel.insertNodeInto(keyNode, valueNode, valueNode.getChildCount());
                renderTraceVariableValue(keyNode, entry.value());
            }
        }
        if (value instanceof FunctionValue) {
            DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(":(function)");
            errorTraceModel.insertNodeInto(valueNode, parent, parent.getChildCount());
            for (FunctionValue.Entry entry : ((FunctionValue) value).entries()) {
                DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(entry.key());
                errorTraceModel.insertNodeInto(keyNode, valueNode, valueNode.getChildCount());
                renderTraceVariableValue(keyNode, entry.value());
            }
        }
        if (value instanceof UnknownValue) {
            DefaultMutableTreeNode valueNode =
                    new DefaultMutableTreeNode(((UnknownValue) value).text());
            errorTraceModel.insertNodeInto(valueNode, parent, parent.getChildCount());
        }
    }

    private static class TableModel extends DefaultTableModel {
        TableModel(String... headers) {
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

    private static class CoverageTableModel extends TableModel {
        CoverageTableModel(String... headers) {
            super(headers);
        }

        public void addRow(CoverageItem coverage) {
            addRow(Arrays.asList(
                    coverage.module(),
                    coverage.action(),
                    String.valueOf(coverage.total()),
                    String.valueOf(coverage.distinct())
            ));
        }

        public void clearRows() {
            while (getRowCount() > 0) {
                removeRow(0);
            }
        }
    }

    private static class StatesTableModel extends TableModel {
        StatesTableModel(String... headers) {
            super(headers);
        }

        private final List<Progress> underlying = new ArrayList<>();

        public void addRow(Progress progress) {
            if (underlying.isEmpty()) {
                underlying.add(progress);
                addRowInternal(underlying.get(0).timestamp(), progress);
            } else {
                int existingIdx = -1;
                for (int i = 0; i < underlying.size(); i++) {
                    if (underlying.get(i).timestamp().equals(progress.timestamp())) {
                        existingIdx = i;
                        break;
                    }
                }
                if (existingIdx >= 0) {
                    underlying.remove(existingIdx);
                    underlying.add(existingIdx, progress);
                    removeRow(existingIdx);
                    addRowInternal(existingIdx, underlying.get(0).timestamp(), progress);
                } else {
                    underlying.add(progress);
                    addRowInternal(underlying.get(0).timestamp(), progress);
                }
            }
        }

        private void addRowInternal(LocalDateTime initTimestamp, Progress progress) {
            addRowInternal(getRowCount(), initTimestamp, progress);
        }

        private void addRowInternal(int idx, LocalDateTime initTimestamp, Progress progress) {
            Duration duration = Duration.between(initTimestamp, progress.timestamp());
            String time = String.format("%02d:%02d:%02d",
                                        duration.toHoursPart(),
                                        duration.toMinutesPart(),
                                        duration.toSecondsPart());
            insertRow(idx, new Vector<>(Arrays.asList(
                    time,
                    String.valueOf(progress.diameter()),
                    String.valueOf(progress.total()),
                    String.valueOf(progress.distinct()),
                    String.valueOf(progress.queueSize())
            )));
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
