package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.testframework.ui.TestResultsPanel;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.UIUtil;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEvent;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEventListener;

/**
 * Split panel which has TLC result as left pane and console-output as right pane.
 * The implementation is mostly taken from {@link TestResultsPanel}
 */
public class TLCResultPanel extends JPanel implements TLCEventListener, Disposable {
    private static final String SPLITTER_PROPERTY = "TLCResult.Splitter.Proportion";

    private JScrollPane leftPane;
    private OnePixelSplitter splitter;
    private TLCModelCheckResultForm modelCheckResultForm;
    private final ConsoleViewImpl consoleView;
    private final TLCTestConsoleProperties properties;
    private final AnAction[] consoleActions;

    public TLCResultPanel(TLCOutputConsoleView consoleView,
                          TLCTestConsoleProperties properties) {
        super(new BorderLayout(0, 1));
        this.properties = properties;
        this.consoleView = consoleView.console();
        JComponent consoleComponent = this.consoleView.getComponent();
        consoleActions = consoleView.consoleActions();
    }

    public void initUI() {
        leftPane = ScrollPaneFactory.createScrollPane();
        leftPane.putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.TOP);
        splitter = createSplitter();
        Disposer.register(this, () -> {
            remove(splitter);
            splitter.dispose();
        });
        splitter.setOpaque(false);
        add(splitter, BorderLayout.CENTER);

        splitter.setFirstComponent(leftPane);

        JComponent console = consoleView.getComponent();
        JPanel rightPanel = new NonOpaquePanel(new BorderLayout());
        console.setFocusable(true);
        Color editorBackground = EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
        console.setBorder(new CompoundBorder(IdeBorderFactory.createBorder(SideBorder.RIGHT),
                                             new SideBorder(editorBackground, SideBorder.LEFT)));
        rightPanel.add(console, BorderLayout.CENTER);
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(
                "TestRunnerResults",
                new DefaultActionGroup(consoleActions),
                false);
        actionToolbar.setTargetComponent(console);
        rightPanel.add(actionToolbar.getComponent(), BorderLayout.EAST);
        splitter.setSecondComponent(rightPanel);

        modelCheckResultForm = new TLCModelCheckResultForm();
        modelCheckResultForm.initUI();
        leftPane.setViewportView(modelCheckResultForm.component());
    }

    @Override
    public void dispose() {
        // noop
    }

    private OnePixelSplitter createSplitter() {
        OnePixelSplitter splitter = new OnePixelSplitter(splitVertically(), SPLITTER_PROPERTY, 0.5f);
        splitter.setHonorComponentsMinimumSize(true);
        return splitter;
    }

    private boolean splitVertically() {
        String windowId = properties.getExecutor().getToolWindowId();
        ToolWindow toolWindow = ToolWindowManager.getInstance(properties.getProject()).getToolWindow(windowId);
        boolean splitVertically = false;
        if (toolWindow != null) {
            ToolWindowAnchor anchor = toolWindow.getAnchor();
            splitVertically = anchor == ToolWindowAnchor.LEFT || anchor == ToolWindowAnchor.RIGHT;
        }
        return splitVertically;
    }

    @Override
    public void onEvent(TLCEvent event) {
        if (event instanceof TLCEvent.TextEvent) {
            consoleView.print(((TLCEvent.TextEvent) event).text(), ConsoleViewContentType.NORMAL_OUTPUT);
        } else {
            modelCheckResultForm.onEvent(event);
        }
    }
}
