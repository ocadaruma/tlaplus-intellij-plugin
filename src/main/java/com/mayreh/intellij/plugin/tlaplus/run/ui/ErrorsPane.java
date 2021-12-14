package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.WrappedPlainView;
import javax.swing.text.html.HTMLEditorKit;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.JBHtmlEditorKit;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.UIUtil.JBWordWrapHtmlEditorKit;

class ErrorsPane extends JTextPane {
    private static final Logger LOG = Logger.getInstance(ErrorsPane.class);

    ErrorsPane() {
//        setEditorKit(new WrapEditorKit());
        setEditable(false);
    }

    public void printLine(String line, Color color) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(StyleConstants.Foreground, color);
        printLineInternal(line, attr);
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

    private static class WrapEditorKit extends StyledEditorKit {
        private final ViewFactory factory = new WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory() {
            return factory;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

    private static class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
//            return new MyView(elem);
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new WrapLabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new ParagraphView(elem);
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                    default:
                        break;
                }
            }

            return new LabelView(elem);
        }
    }

    private static class MyView extends WrappedPlainView {
        public MyView(Element elem) {
            super(elem, true);
        }

        @Override
        protected void drawLine(int p0, int p1, Graphics2D g, float x, float y) {
            super.drawLine(p0, p1, g, x, y);
        }

        @Override
        public float getPreferredSpan(int axis) {
            if (axis == View.X_AXIS) {
                return 0;
            }
            return super.getPreferredSpan(axis);
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (axis == View.X_AXIS) {
                return 0;
            }
            return super.getMinimumSpan(axis);
        }
    }

    private static class WrapLabelView extends LabelView {
        WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (axis == View.X_AXIS) {
                return 0;
            }
            return super.getMinimumSpan(axis);
        }

        //        @Override
//        public float getPreferredSpan(int axis) {
//            if (axis == View.X_AXIS) {
//                return 0;
//            }
//            return super.getPreferredSpan(axis);
//        }
    }
}
