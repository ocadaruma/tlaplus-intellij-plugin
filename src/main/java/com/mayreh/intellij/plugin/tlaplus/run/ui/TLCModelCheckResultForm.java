package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.AnimatedIcon.Default;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
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
//        JScrollPane errorsScrollPane = ScrollPaneFactory
//                .createScrollPane(errorsPane,
//                                  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
//                                  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//        errorsScrollPane.setBorder(Borders.empty());
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
                errorsPane.printLine(
                        error.message(),
                        ((TLCError) event).severity() == Severity.Error ?
                        ColorProgressBar.RED_TEXT : ColorProgressBar.YELLOW);
//                errorsPane.setText("aowiuerij;l a;sifj;aoweh ;oiwhe;roi ;jas;oeiuroiweur;oiasje;oirja;soiehr;alksejr;os;aoiejr;is;aoeirj;aowiuerij;l a;sifj;aoweh ;oiwhe;roi ;jas;oeiuroiweur;oiasje;oirja;soiehr;alksejr;os;aoiejr;is;aoeirj;aowiuerij;l a;sifj;aoweh ;oiwhe;roi ;jas;oeiuroiweur;oiasje;oirja;soiehr;alksejr;os;aoiejr;is;aoeirj;");
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
        panel = new ScrollablePanel();
        panel.setLayout(new GridLayoutManager(11, 1, JBUI.emptyInsets(), -1, -1));
    }

//    {
//// GUI initializer generated by IntelliJ IDEA GUI Designer
//// >>> IMPORTANT!! <<<
//// DO NOT EDIT OR ADD ANY CODE HERE!
//        $$$setupUI$$$();
//    }
//
//    /** Method generated by IntelliJ IDEA GUI Designer
//     * >>> IMPORTANT!! <<<
//     * DO NOT edit this method OR call it in your code!
//     * @noinspection ALL
//     */
//    private void $$$setupUI$$$() {
//        panel = new JPanel();
//        panel.setLayout(new GridLayoutManager(11, 1, new Insets(0, 0, 0, 0), -1, -1));
//        final TitledSeparator titledSeparator1 = new TitledSeparator();
//        titledSeparator1.setText("General");
//        panel.add(titledSeparator1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
//                                                        GridConstraints.FILL_HORIZONTAL,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
//                                                        1, false));
//        final Spacer spacer1 = new Spacer();
//        panel.add(spacer1,
//                  new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
//                                      1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
//        final JPanel panel1 = new JPanel();
//        panel1.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
//        panel.add(panel1,
//                  new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
//        final JLabel label1 = new JLabel();
//        label1.setText("Status:");
//        panel1.add(label1,
//                   new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
//                                       GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
//                                       null, null, 0, false));
//        statusLabel = new JLabel();
//        statusLabel.setText("");
//        panel1.add(statusLabel,
//                   new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
//                                       GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
//                                       null, null, null, 0, false));
//        final JLabel label2 = new JLabel();
//        label2.setText("Start:");
//        panel1.add(label2,
//                   new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
//                                       GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
//                                       null, null, 0, false));
//        startLabel = new JLabel();
//        startLabel.setText("");
//        panel1.add(startLabel,
//                   new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
//                                       GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
//                                       null, null, null, 0, false));
//        final JLabel label3 = new JLabel();
//        label3.setText("End:");
//        panel1.add(label3,
//                   new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
//                                       GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
//                                       null, null, 0, false));
//        endLabel = new JLabel();
//        endLabel.setText("");
//        panel1.add(endLabel,
//                   new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
//                                       GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
//                                       null, null, 0, false));
//        final TitledSeparator titledSeparator2 = new TitledSeparator();
//        titledSeparator2.setText("States");
//        panel.add(titledSeparator2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
//                                                        GridConstraints.FILL_HORIZONTAL,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
//                                                        1, false));
//        final TitledSeparator titledSeparator3 = new TitledSeparator();
//        titledSeparator3.setText("Coverage");
//        panel.add(titledSeparator3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
//                                                        GridConstraints.FILL_HORIZONTAL,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
//                                                        1, false));
//        final TitledSeparator titledSeparator4 = new TitledSeparator();
//        titledSeparator4.setText("Errors");
//        panel.add(titledSeparator4, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
//                                                        GridConstraints.FILL_HORIZONTAL,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
//                                                        1, false));
//        final TitledSeparator titledSeparator5 = new TitledSeparator();
//        titledSeparator5.setText("Error Trace");
//        panel.add(titledSeparator5, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
//                                                        GridConstraints.FILL_HORIZONTAL,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                                        | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
//                                                        1, false));
//        statesTablePanel = new JPanel();
//        statesTablePanel.setLayout(new BorderLayout(0, 0));
//        panel.add(statesTablePanel,
//                  new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
//        statesTablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null,
//                                                                    TitledBorder.DEFAULT_JUSTIFICATION,
//                                                                    TitledBorder.DEFAULT_POSITION, null, null));
//        coverageTablePanel = new JPanel();
//        coverageTablePanel.setLayout(new BorderLayout(0, 0));
//        panel.add(coverageTablePanel,
//                  new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
//        coverageTablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null,
//                                                                      TitledBorder.DEFAULT_JUSTIFICATION,
//                                                                      TitledBorder.DEFAULT_POSITION, null,
//                                                                      null));
//        errorsPanel = new JPanel();
//        errorsPanel.setLayout(new BorderLayout(0, 0));
//        panel.add(errorsPanel,
//                  new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
//        errorsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null,
//                                                               TitledBorder.DEFAULT_JUSTIFICATION,
//                                                               TitledBorder.DEFAULT_POSITION, null, null));
//        errorTracePanel = new JPanel();
//        errorTracePanel.setLayout(new BorderLayout(0, 0));
//        panel.add(errorTracePanel,
//                  new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW,
//                                      GridConstraints.SIZEPOLICY_CAN_SHRINK
//                                      | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
//        errorTracePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null,
//                                                                   TitledBorder.DEFAULT_JUSTIFICATION,
//                                                                   TitledBorder.DEFAULT_POSITION, null, null));
//    }
//
//    /** @noinspection ALL */
//    public JComponent $$$getRootComponent$$$() {return panel;}

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
