package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import static com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace.ErrorTraceModel.NAME_COLUMN_INDEX;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.JBUI.Borders;
import com.mayreh.intellij.plugin.util.Optionalx;

import lombok.RequiredArgsConstructor;

/**
 * {@link TableCellRenderer} impl that renders a separator for each state for better visibility
 */
@RequiredArgsConstructor
class StateSeparatingCellRenderer implements TableCellRenderer {
    private final TableCellRenderer delegate;
    /**
     * Same color as {@link TitledSeparator}
     */
    private static final JBColor SEPARATOR_COLOR = new JBColor(Gray.xCD, Gray.x51);

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        Component component = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Optionalx.asInstanceOf(component, JComponent.class)
                 .ifPresent(jComponent -> {
                     // This cell is at the first row of each state when NAME column's value is StateRootNode.
                     // We do not want to render separator for first state to avoid colliding with header.
                     if (table.getValueAt(row, NAME_COLUMN_INDEX) instanceof StateRootNode &&
                         row > 0 &&
                         !isSelected) {
                         jComponent.setBorder(Borders.customLineTop(SEPARATOR_COLOR));
                     } else {
                         jComponent.setBorder(Borders.empty());
                     }
                 });
        return component;
    }
}
