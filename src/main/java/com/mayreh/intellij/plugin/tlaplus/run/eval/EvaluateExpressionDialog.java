package com.mayreh.intellij.plugin.tlaplus.run.eval;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.intellij.xdebugger.impl.evaluate.CodeFragmentInputComponent;
import com.intellij.xdebugger.impl.evaluate.EvaluationInputComponent;
import com.intellij.xdebugger.impl.evaluate.ExpressionInputComponent;
import com.intellij.xdebugger.impl.evaluate.XDebuggerEvaluationDialog;
import com.intellij.xdebugger.impl.settings.XDebuggerSettingManagerImpl;
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase;

/**
 * Almost taken from {@link XDebuggerEvaluationDialog}
 */
public class EvaluateExpressionDialog extends DialogWrapper {
    private final Project project;
    private final @Nullable Context context;
    private final XDebuggerEditorsProvider editorsProvider;
    private final SwitchModeAction switchModeAction;
    private final JPanel mainPanel;
    private final JPanel resultPanel;
    private final JTextPane resultTextPane;
    private EvaluationMode mode;
    private EvaluationInputComponent inputComponent;

    public EvaluateExpressionDialog(@NotNull Project project,
                                    @Nullable Context context,
                                    @NotNull XDebuggerEditorsProvider editorsProvider,
                                    @NotNull XExpression text) {
        super(project, true);

        this.project = project;
        this.context = context;
        this.editorsProvider = editorsProvider;
        setModal(false);
        setOKButtonText(XDebuggerBundle.message("xdebugger.button.evaluate"));
        setCancelButtonText(XDebuggerBundle.message("xdebugger.evaluate.dialog.close"));

        resultTextPane = new JTextPane();
        resultTextPane.setEditable(false);
        resultTextPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        resultPanel = JBUI.Panels
                .simplePanel()
                .addToCenter(resultTextPane);
        resultPanel.setBorder(BorderFactory.createTitledBorder("Result"));
        mainPanel = new MainPanel();
        switchModeAction = new SwitchModeAction();
        EvaluationMode mode = XDebuggerSettingManagerImpl.getInstanceImpl().getGeneralSettings().getEvaluationDialogMode();
        if (mode == EvaluationMode.EXPRESSION && text.getMode() == EvaluationMode.CODE_FRAGMENT) {
            mode = EvaluationMode.CODE_FRAGMENT;
        }
        setTitle(XDebuggerBundle.message("xdebugger.evaluate.dialog.title"));
        switchToMode(mode, text);

        if (mode == EvaluationMode.EXPRESSION) {
            inputComponent.getInputEditor().selectAll();
        }
        init();
    }

    @Override
    protected String getDimensionServiceKey() {
        return "#xdebugger.evaluate";
    }

    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        evaluate();
    }

    @Override
    public void doCancelAction() {
        inputComponent.getInputEditor().saveTextInHistory();
        super.doCancelAction();
    }

    @Override
    protected JButton createJButtonForAction(Action action) {
        JButton button = super.createJButtonForAction(action);
        if (action == switchModeAction) {
            Dimension size = new Dimension(Math.max(
                    new JButton(switchButtonText(EvaluationMode.EXPRESSION)).getPreferredSize().width,
                    new JButton(switchButtonText(EvaluationMode.CODE_FRAGMENT)).getPreferredSize().width
            ), button.getPreferredSize().height);
            button.setMinimumSize(size);
            button.setPreferredSize(size);
        }
        return button;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return inputComponent.getInputEditor().getPreferredFocusedComponent();
    }

    private void switchToMode(EvaluationMode mode, XExpression text) {
        if (this.mode == mode) {
            return;
        }
        this.mode = mode;

        Editor oldEditor = (inputComponent != null) ? inputComponent.getInputEditor().getEditor() : null;

        inputComponent = createInputComponent(mode, text);
        mainPanel.removeAll();
        inputComponent.addComponent(mainPanel, resultPanel);

        XDebuggerEditorBase.copyCaretPosition(oldEditor, inputComponent.getInputEditor().getEditor());

        switchModeAction.putValue(Action.NAME, switchButtonText(mode));
        inputComponent.getInputEditor().requestFocusInEditor();
    }

    private EvaluationInputComponent createInputComponent(EvaluationMode mode, XExpression text) {
        text = XExpressionImpl.changeMode(text, mode);
        if (mode == EvaluationMode.EXPRESSION) {
            ExpressionInputComponent component = new ExpressionInputComponent(
                    project,
                    editorsProvider,
                    "evaluateExpression", null,
                    text, myDisposable,
                    false);
            component.getInputEditor().setExpandHandler(() -> switchModeAction.actionPerformed(null));
            return component;
        }
        else {
            CodeFragmentInputComponent component = new CodeFragmentInputComponent(
                    project,
                    editorsProvider,
                    null, text,
                    getDimensionServiceKey() + ".splitter", myDisposable);
            component.getInputEditor().addCollapseButton(() -> switchModeAction.actionPerformed(null));
            return component;
        }
    }

    private void evaluate() {
        XDebuggerEditorBase inputEditor = inputComponent.getInputEditor();
        inputEditor.saveTextInHistory();

        if (inputEditor.getExpression() != null) {
            resultTextPane.setText("Evaluating...");

            String expression = inputEditor.getExpression().getExpression();
            ReadAction.nonBlocking(() -> ExpressionEvaluator.evaluate(context, expression))
                      .finishOnUiThread(ModalityState.defaultModalityState(), result -> {
                          StyledDocument document = resultTextPane.getStyledDocument();
                          try {
                              document.remove(0, document.getLength());
                              if (StringUtil.isNotEmpty(result.output())) {
                                  document.insertString(document.getLength(), result.output(), null);
                              }
                              if (!result.errors().isEmpty()) {
                                  if (document.getLength() > 0) {
                                      document.insertString(document.getLength(), "\n", null);
                                  }
                                  for (int i = 0; i < result.errors().size(); i++) {
                                      if (i > 0) {
                                          document.insertString(document.getLength(), "\n", null);
                                      }
                                      SimpleAttributeSet attr = new SimpleAttributeSet();
                                      attr.addAttribute(StyleConstants.Foreground, ColorProgressBar.RED_TEXT);
                                      document.insertString(document.getLength(), result.errors().get(i), attr);
                                  }
                              }
                          } catch (BadLocationException e) {
                              throw new RuntimeException(e);
                          }
                      }).expireWith(myDisposable).submit(AppExecutorUtil.getAppExecutorService());
        }
    }

    private static @Nls String switchButtonText(EvaluationMode mode) {
        return mode != EvaluationMode.EXPRESSION
               ? XDebuggerBundle.message("button.text.expression.mode")
               : XDebuggerBundle.message("button.text.code.fragment.mode");
    }

    private class SwitchModeAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            XExpression text = inputComponent.getInputEditor().getExpression();
            EvaluationMode newMode = (mode == EvaluationMode.EXPRESSION) ? EvaluationMode.CODE_FRAGMENT : EvaluationMode.EXPRESSION;
            XDebuggerSettingManagerImpl.getInstanceImpl().getGeneralSettings().setEvaluationDialogMode(newMode);
            switchToMode(newMode, text);
        }
    }

    private static class MainPanel extends BorderLayoutPanel {
        @Override
        public Dimension getMinimumSize() {
            Dimension size = super.getMinimumSize();
            size.width = Math.max(size.width, JBUI.scale(450));
            return size;
        }
    }
}
