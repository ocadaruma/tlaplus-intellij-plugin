package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTable;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.TextIcon;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.table.IconTableCellRenderer;

class ValueColumnCellRenderer extends IconTableCellRenderer<TraceVariableValueWrapper> {
    @Override
    protected Icon getIcon(@NotNull TraceVariableValueWrapper value, JTable table, int row) {
        final String text;
        final Color foreground;
        switch (value.diffType()) {
            case Modified:
                text = "M";
                // We prefer UNKNOWN rather than FileStatus.MODIFIED for better visibility
                foreground = FileStatus.MODIFIED.getColor();
                break;
            case Added:
                text = "A";
                foreground = FileStatus.ADDED.getColor();
                break;
            default:
                // We fill dummy text even when unchanged to allocate same size of rectangle
                text = "_";
                foreground = UIUtil.TRANSPARENT_COLOR;
                break;
        }
        TextIcon icon = new TextIcon(text, foreground, null, 1);
        icon.setFont(RelativeFont.BOLD.family(Font.MONOSPACED).derive(getFont()));
        return icon;
    }
}
