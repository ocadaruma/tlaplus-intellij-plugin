package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.SourceLocation;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent.CoverageItem;

import lombok.Value;
import lombok.experimental.Accessors;

class TLCCoverageTableModel extends DefaultTableModel {
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
                new ActionLocation(coverage.module(), coverage.action(), coverage.range().getFrom()),
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
        if (value instanceof ActionLocation) {
            return ((ActionLocation) value).action();
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

@Value
@Accessors(fluent = true)
class ActionLocation {
    String module;
    String action;
    SourceLocation location;
}

class TLCCoverageTable extends JBTable {
    private final TLCCoverageTableModel tableModel;

    TLCCoverageTable(TLCCoverageTableModel tableModel) {
        super(tableModel);
        this.tableModel = tableModel;
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
                Object rawValue = tableModel.rawValueAt(row, column);
                if (rawValue instanceof ActionLocation) {
                    ActionLocation loc = (ActionLocation) rawValue;
                    return getActionLocationRendererComponent(isSelected, loc);
                }
                Component component = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                setBorder(noFocusBorder);
                return component;
            }

            /**
             * Implement almost same logic as {@link DefaultTableCellRenderer#getTableCellRendererComponent}
             * for highlighting when selected
             */
            private Component getActionLocationRendererComponent(boolean isSelected, ActionLocation loc) {
                SimpleColoredComponent component = new SimpleColoredComponent();
                int attr = SimpleTextAttributes.REGULAR_ATTRIBUTES.getStyle() | SimpleTextAttributes.STYLE_UNDERLINE;

                final Color foreground;
                if (isSelected) {
                    foreground = getSelectionForeground();
                    component.setBackground(getSelectionBackground());
                } else {
                    foreground = JBUI.CurrentTheme.Link.Foreground.ENABLED;
                    component.setBackground(TLCCoverageTable.this.getBackground());
                }
                component.setFont(TLCCoverageTable.this.getFont());
                component.append(loc.action(), new SimpleTextAttributes(attr, foreground));

                return component;
            }
        };
    }
}
