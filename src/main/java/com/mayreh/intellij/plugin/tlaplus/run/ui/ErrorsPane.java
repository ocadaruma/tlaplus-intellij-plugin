package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.intellij.openapi.diagnostic.Logger;

class ErrorsPane extends JTextPane {
    private static final Logger LOG = Logger.getInstance(ErrorsPane.class);

    ErrorsPane() {
        setEditable(false);
    }

    public void printLine(String line, Color color) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(StyleConstants.Foreground, color);
        printLineInternal(line, attr);
    }

    public void printLine(String line) {
        printLineInternal(line, null);
    }

    private void printLineInternal(String line, AttributeSet attr) {
        Document doc = getDocument();
        String prefix = doc.getLength() > 0 ? "\n" : "";
        try {
            doc.insertString(doc.getLength(), prefix + line, attr);
        } catch (BadLocationException e) {
            LOG.warn(e);
        }
    }
}
