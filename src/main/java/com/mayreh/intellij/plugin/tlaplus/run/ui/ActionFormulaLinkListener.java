package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ui.UIUtil;
import com.mayreh.intellij.plugin.tlaplus.psi.TLAplusModule;

public abstract class ActionFormulaLinkListener extends MouseAdapter {
    private final @Nullable TLAplusModule module;

    protected ActionFormulaLinkListener(@Nullable TLAplusModule module) {
        this.module = module;
    }

    private Cursor lastCursor = null;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        ActionFormula formula = taggedActionFormula(e);
        if (formula != null && e.getClickCount() == 1) {
            Optional.ofNullable(module)
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
        JComponent component = (JComponent)e.getSource();
        if (taggedActionFormula(e) != null) {
            swapCursor(component);
        } else {
            restoreCursor(component);
        }
    }

    @Nullable
    protected abstract ActionFormula taggedActionFormula(MouseEvent e);

    private void swapCursor(JComponent component) {
        if (component.getCursor().getType() != Cursor.HAND_CURSOR && lastCursor == null) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            lastCursor = component.getCursor();
            component.setCursor(cursor);
        }
    }

    private void restoreCursor(JComponent component) {
        if (component.getCursor().getType() != Cursor.DEFAULT_CURSOR) {
            component.setCursor(UIUtil.cursorIfNotDefault(lastCursor));
        }
        lastCursor = null;
    }
}
