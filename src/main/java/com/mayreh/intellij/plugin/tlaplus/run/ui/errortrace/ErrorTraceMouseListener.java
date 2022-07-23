package com.mayreh.intellij.plugin.tlaplus.run.ui.errortrace;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;

class ErrorTraceMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JBPopupMenu.showByEvent(e, createPopup());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JBPopupMenu.showByEvent(e, createPopup());
        }
    }

    private JBPopupMenu createPopup() {
        JBPopupMenu popup = new JBPopupMenu();

        JBMenuItem expand = new JBMenuItem("Expand");
        expand.addActionListener(e -> System.out.println("expand"));
        popup.add(expand);

        JBMenuItem hide = new JBMenuItem("Hide");
        hide.addActionListener(e -> System.out.println("hide"));
        popup.add(hide);

        JBMenuItem reset = new JBMenuItem("Reset");
        reset.addActionListener(e -> System.out.println("reset"));
        popup.add(reset);
        return popup;
    }
}
