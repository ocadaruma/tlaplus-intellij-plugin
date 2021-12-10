package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageItem;

import lombok.RequiredArgsConstructor;

class TLCCoverageTable extends JBTable {
    private final TLCCoverageTableModel tableModel;

    TLCCoverageTable(TLCCoverageTableModel tableModel,
                     @Nullable TLAplusModule module) {
        super(tableModel);
        this.tableModel = tableModel;
        setCellSelectionEnabled(true);

        ActionFormulaLinkListener mouseListener = new ActionFormulaLinkListener(module) {
            @Override
            protected @Nullable ActionFormula taggedActionFormula(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int column = columnAtPoint(e.getPoint());

                if (row < 0 || column < 0) {
                    return null;
                }

                TableCellRenderer renderer = getCellRenderer(row, column);
                if (renderer instanceof SimpleColoredComponent) {
                    // The logic is taken from com.intellij.openapi.vcs.changes.issueLinks.TableLinkMouseListener
                    renderer.getTableCellRendererComponent(
                            TLCCoverageTable.this, getValueAt(row, column), false, false, row, column);
                    Object tag = ((SimpleColoredComponent) renderer).getFragmentTagAt(
                            e.getX() - getCellRect(row, column, false).x);
                    if (tag instanceof ActionFormula) {
                        return (ActionFormula) tag;
                    }
                }
                return null;
            }
        };
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
