package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.UIUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageItem;

import lombok.RequiredArgsConstructor;

class TLCCoverageTable extends JBTable {
    private final TLCCoverageTableModel tableModel;
    private final @Nullable TLAplusModule module;

    TLCCoverageTable(TLCCoverageTableModel tableModel,
                     @Nullable TLAplusModule module) {
        super(tableModel);
        this.tableModel = tableModel;
        this.module = module;
        setCellSelectionEnabled(true);

        TableMouseAdapter mouseListener = new TableMouseAdapter(this);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(getPreferredSize().width, getRowHeight() * getRowCount());
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return new CoverageTableCellRenderer(tableModel);
    }

    @RequiredArgsConstructor
    static class TableMouseAdapter extends MouseAdapter {
        private final TLCCoverageTable table;
        private Cursor lastCursor = null;

        @Override
        public void mouseClicked(MouseEvent e) {
            ActionFormula formula = actionTag(e);
            if (formula != null && e.getClickCount() == 1) {
                Optional.ofNullable(table.module)
                        .flatMap(module -> {
                            if (formula.module().equals(module.getModuleHeader().getName())) {
                                return Optional.ofNullable(module.getContainingFile())
                                               .flatMap(file -> Optional.ofNullable(file.getVirtualFile()))
                                               .map(file -> Pair.pair(module, file));
                            }
                            return module.availableModules()
                                         .filter(m -> formula.module().equals(m.getModuleHeader().getName()))
                                         .findFirst()
                                         .flatMap(m -> Optional
                                                 .ofNullable(m.getContainingFile())
                                                 .flatMap(file -> Optional.ofNullable(file.getVirtualFile())))
                                         .map(file -> Pair.pair(module, file));
                        })
                        .ifPresent(pair -> {
                            new OpenFileDescriptor(
                                    pair.first.getProject(),
                                    pair.second, formula.location().line(), formula.location().col()
                            ).navigate(true);
                        });
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (actionTag(e) != null) {
                swapCursor();
            } else {
                restoreCursor();
            }
        }

        private @Nullable ActionFormula actionTag(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            int column = table.columnAtPoint(e.getPoint());

            if (row < 0 || column < 0) {
                return null;
            }

            TableCellRenderer renderer = table.getCellRenderer(row, column);
            if (renderer instanceof CoverageTableCellRenderer) {
                // The logic is taken from com.intellij.openapi.vcs.changes.issueLinks.TableLinkMouseListener
                renderer.getTableCellRendererComponent(
                        table, table.getValueAt(row, column), false, false, row, column);
                Object tag = ((CoverageTableCellRenderer) renderer).getFragmentTagAt(
                        e.getX() - table.getCellRect(row, column, false).x);
                if (tag instanceof ActionFormula) {
                    return (ActionFormula) tag;
                }
            }
            return null;
        }

        private void swapCursor() {
            if (table.getCursor().getType() != Cursor.HAND_CURSOR && lastCursor == null) {
                Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                lastCursor = table.getCursor();
                table.setCursor(cursor);
            }
        }

        private void restoreCursor() {
            if (table.getCursor().getType() != Cursor.DEFAULT_CURSOR) {
                table.setCursor(UIUtil.cursorIfNotDefault(lastCursor));
            }
            lastCursor = null;
        }
    }

    static class TLCCoverageTableModel extends DefaultTableModel {
        TLCCoverageTableModel() {
            super(new Vector<>(Arrays.asList("Module", "Action", "Total", "Distinct")), 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public void addRow(CoverageItem coverage) {
            addRow(new Vector<>(Arrays.asList(
                    coverage.module(),
                    new ActionFormula(coverage.module(), coverage.action(), coverage.range().getFrom()),
                    coverage.total(),
                    coverage.distinct()
            )));
        }

        public void clearRows() {
            while (getRowCount() > 0) {
                removeRow(0);
            }
        }

        /**
         * Override the value for copy&paste
         */
        @Override
        public Object getValueAt(int row, int column) {
            Object value = rawValueAt(row, column);
            if (value instanceof ActionFormula) {
                return ((ActionFormula) value).action();
            }
            return value;
        }

        /**
         * Define separately from {@link #getValueAt(int, int)} to retrieve original
         * coverage item (for 2nd column)
         */
        public Object rawValueAt(int row, int column) {
            return dataVector.elementAt(row).elementAt(column);
        }
    }

    @RequiredArgsConstructor
    static class CoverageTableCellRenderer extends ColoredTableCellRenderer {
        private final TLCCoverageTableModel tableModel;

        @Override
        protected void customizeCellRenderer(
                @NotNull JTable table, @Nullable Object value, boolean selected, boolean hasFocus, int row, int column) {
            Object rawValue = tableModel.rawValueAt(row, column);

            final Color unselectedForeground;
            final int attributes;
            final String text;
            final Object tag;
            if (rawValue instanceof ActionFormula) {
                ActionFormula formula = (ActionFormula) rawValue;
                tag = rawValue;
                unselectedForeground = JBUI.CurrentTheme.Link.Foreground.ENABLED;
                attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES.getStyle() | SimpleTextAttributes.STYLE_UNDERLINE;
                text = formula.action();
            } else {
                tag = null;
                unselectedForeground = table.getForeground();
                attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES.getStyle();
                text = value == null ? "" : value.toString();
            }

            if (selected) {
                setBackground(table.getSelectionBackground());
                append(text, new SimpleTextAttributes(attributes, table.getSelectionForeground()), tag);
            } else {
                setBackground(table.getBackground());
                append(text, new SimpleTextAttributes(attributes, unselectedForeground), tag);
            }

            // common
            setFont(table.getFont());
            setBorder(Borders.empty());
        }
    }
}
