package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import java.awt.Color;
import java.util.List;

import javax.swing.JTree;

import org.jetbrains.annotations.NotNull;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.JBUI.Borders;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ErrorTraceCellRenderer extends ColoredTreeCellRenderer {
    private final TreeTable treeTable;

    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        if (!(value instanceof StateRootNode)) {
            append(value == null ? "" : value.toString());
            return;
        }
        List<TextFragment> fragments = ((StateRootNode) value).textRepresentation();
        for (TextFragment fragment : fragments) {
            final Color unselectedForeground;
            final int attributes;
            if (fragment.actionFormula() != null) {
                unselectedForeground = JBUI.CurrentTheme.Link.Foreground.ENABLED;
                attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES.getStyle() | SimpleTextAttributes.STYLE_UNDERLINE;
            } else {
                unselectedForeground = treeTable.getForeground();
                attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES.getStyle();
            }

            final Color foreground;
            if (selected) {
                foreground = treeTable.getSelectionForeground();
            } else {
                foreground = unselectedForeground;
            }

            append(fragment.text(), new SimpleTextAttributes(attributes, foreground), fragment.actionFormula());
        }

        // common
        setFont(tree.getFont());
        setBorder(Borders.empty());
    }
}
