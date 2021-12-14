package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel;
import com.intellij.ui.AnimatedIcon.Default;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBFont;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageInit;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageItem;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageNext;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.BackToStateErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SimpleErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ErrorTraceEvent.SpecialErrorTrace;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.ProcessTerminated;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.Progress;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.SANYEnd;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.SANYError;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.SANYStart;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCError;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCError.ErrorItem;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCError.Severity;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCFinished;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCStart;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.TLCSuccess;
import com.mayreh.intellij.plugin.tlaplus.run.ui.ErrorTraceTreeTable.StateRootNode;
import com.mayreh.intellij.plugin.tlaplus.run.ui.TLCCoverageTable.TLCCoverageTableModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TLCModelCheckResultForm {
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    private final TLCTestConsoleProperties properties;

    private JPanel panel;
    private JLabel statusLabel;
    private JLabel startLabel;
    private JLabel endLabel;
    private ErrorTraceTreeTable errorTraceTree;
    private JPanel statesTablePanel;
    private JPanel coverageTablePanel;
    private JPanel errorsPanel;
    private JPanel errorTracePanel;
    private StatesTableModel statesTableModel;
    private TLCCoverageTableModel coverageTableModel;
    private ErrorsPane errorsPane;

    // We want to set statusLabel from exitCode=0 event only when
    // TLCFinished event is not received yet. (though not sure if such case can happen)
    private boolean statusSet = false;

    public JComponent component() {
        return panel;
    }

    public void initUI() {
        errorsPane = new ErrorsPane();
        errorsPanel.add(errorsPane, BorderLayout.CENTER);

        statesTableModel = new StatesTableModel("Time", "Diameter", "Found", "Distinct", "Queue");
        JTable statesTable = new SimpleTable(statesTableModel);
        statesTablePanel.add(statesTable.getTableHeader(), BorderLayout.NORTH);
        statesTablePanel.add(statesTable, BorderLayout.CENTER);

        coverageTableModel = new TLCCoverageTableModel();
        JTable coverageTable = new TLCCoverageTable(coverageTableModel, properties.module());
        coverageTablePanel.add(coverageTable.getTableHeader(), BorderLayout.NORTH);
        coverageTablePanel.add(coverageTable, BorderLayout.CENTER);

        errorTraceTree = new ErrorTraceTreeTable(properties.module());
        errorTracePanel.add(errorTraceTree.getTableHeader(), BorderLayout.NORTH);
        errorTracePanel.add(errorTraceTree, BorderLayout.CENTER);

        List<JTable> tables = Arrays.asList(statesTable, coverageTable, errorTraceTree);
        // Make cell-selection exclusive among all tables (which should be familiar behavior)
        ListSelectionListener selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                for (JTable table : tables) {
                    ListSelectionModel model = table.getSelectionModel();
                    if (e.getSource() != model) {
                        model.removeListSelectionListener(this);
                        model.clearSelection();
                        model.addListSelectionListener(this);
                    }
                }
            }
        };
        for (JTable table : tables) {
            table.getSelectionModel().addListSelectionListener(selectionListener);
        }
    }

    public void onEvent(TLCEvent event) {
        if (event instanceof SANYStart) {
            statusLabel.setText("SANY running");
        }
        if (event instanceof SANYEnd) {
            statusLabel.setText("SANY finished");
            for (SANYError sanyError : ((SANYEnd) event).errors()) {
                String message = String.format(
                        "%s(%d:%d): %s",
                        sanyError.module(),
                        sanyError.location().line(),
                        sanyError.location().col(),
                        sanyError.message());

                errorsPane.printLine(message, ColorProgressBar.RED_TEXT);
            }
        }
        if (event instanceof TLCStart) {
            statusLabel.setIcon(Default.INSTANCE);
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
                if (((TLCError) event).severity() == Severity.Error) {
                    errorsPane.printLine(error.message(), ColorProgressBar.RED_TEXT);
                } else {
                    errorsPane.printLine(error.message());
                }
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
            errorTraceTree.expandStates();
        }
        if (event instanceof SimpleErrorTrace) {
            SimpleErrorTrace trace = (SimpleErrorTrace) event;
            errorTraceTree.addState(new StateRootNode(trace), trace.variables());
        }
        if (event instanceof SpecialErrorTrace) {
            SpecialErrorTrace trace = (SpecialErrorTrace) event;
            errorTraceTree.addState(new StateRootNode(trace), trace.variables());
        }
        if (event instanceof BackToStateErrorTrace) {
            BackToStateErrorTrace trace = (BackToStateErrorTrace) event;
            errorTraceTree.addState(new StateRootNode(trace), trace.variables());
        }
    }

    private void createUIComponents() {
        // We need to instantiate the root panel as `Scrollable` so that outer scroll pane (i.e. TLCResultPanel's splitter left pane)
        // can resize this panel to fit in view port with taking `Scrollable#getScrollableTracksViewportWidth` into account.
        panel = new ScrollablePanel();
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
            setCellSelectionEnabled(true);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(getPreferredSize().width, getRowHeight() * getRowCount());
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            return new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component component = super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
                    setBorder(noFocusBorder);
                    return component;
                }
            };
        }
    }
}
