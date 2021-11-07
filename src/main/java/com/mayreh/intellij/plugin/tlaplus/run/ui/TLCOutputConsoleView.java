package com.mayreh.intellij.plugin.tlaplus.run.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.actions.ClearConsoleAction;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.HelpIdProvider;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.psi.search.GlobalSearchScope;
import com.mayreh.intellij.plugin.tlaplus.run.parsing.TLCEventParser;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * The implementation is mostly taken from {@link BaseTestsOutputConsoleView}
 */
public class TLCOutputConsoleView implements ConsoleView, HelpIdProvider {
    interface State {
        @NotNull State dispose();
        @NotNull State attachTo(TLCOutputConsoleView consoleView, ProcessHandler processHandler);

        class NotRunning implements State {
            @Override
            public @NotNull State dispose() {
                return this;
            }

            @Override
            public @NotNull State attachTo(TLCOutputConsoleView consoleView, ProcessHandler processHandler) {
                return new Running(consoleView, processHandler);
            }
        }

        class Running implements State {
            private final ProcessHandler processHandler;
            private final NotRunning finishedState = new NotRunning();
            private final ProcessListener listener;

            public Running(TLCOutputConsoleView consoleView, ProcessHandler processHandler) {
                this.processHandler = processHandler;
                listener = new ProcessAdapter() {
                    private TLCEventParser currentParser = TLCEventParser.create(
                            consoleView.resultPanel, consoleView.console().getProject());
                    @Override
                    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                        ApplicationManager.getApplication().invokeLater(
                                () -> currentParser = currentParser.addLine(event.getText()));
                    }

                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        ApplicationManager.getApplication().invokeLater(
                                () -> currentParser.notifyProcessExit(event.getExitCode()));

                    }
                };
                processHandler.addProcessListener(listener);
            }

            @Override
            public @NotNull State dispose() {
                processHandler.removeProcessListener(listener);
                return finishedState;
            }

            @Override
            public @NotNull State attachTo(TLCOutputConsoleView consoleView, ProcessHandler processHandler) {
                return dispose().attachTo(consoleView, processHandler);
            }
        }
    }

    @Getter
    @Accessors(fluent = true)
    private ConsoleViewImpl console;
    private State state = new State.NotRunning();
    private final TLCResultPanel resultPanel;

    public TLCOutputConsoleView(TLCTestConsoleProperties properties) {
        console = new ConsoleViewImpl(
                properties.getProject(),
                GlobalSearchScope.allScope(properties.getProject()),
                false,
                true);
        properties.setConsole(this);

        Disposer.register(this, properties);
        Disposer.register(this, console);

        resultPanel = new TLCResultPanel(this, properties);
        resultPanel.initUI();
        Disposer.register(this, resultPanel);
    }

    @Override
    public void print(@NotNull String text, @NotNull ConsoleViewContentType contentType) {
        // noop as all outputs go to TLCResultPanel and routed to underlying ConsoleView as necessary
    }

    @Override
    public void clear() {
        console.clear();
    }

    @Override
    public void scrollTo(int offset) {
        console.scrollTo(offset);
    }

    @Override
    public void attachToProcess(@NotNull ProcessHandler processHandler) {
        state.attachTo(this, processHandler);
    }

    // We don't support pausing the console
    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public void setOutputPaused(boolean value) {
        // noop
    }

    @Override
    public boolean isOutputPaused() {
        return false;
    }

    @Override
    public boolean hasDeferredOutput() {
        return console.hasDeferredOutput();
    }

    @Override
    public void performWhenNoDeferredOutput(@NotNull Runnable runnable) {
        console.performWhenNoDeferredOutput(runnable);
    }

    @Override
    public void setHelpId(@NotNull String helpId) {
        console.setHelpId(helpId);
    }

    @Override
    public void addMessageFilter(@NotNull Filter filter) {
        console.addMessageFilter(filter);
    }

    @Override
    public void printHyperlink(@NotNull String hyperlinkText, @Nullable HyperlinkInfo info) {
        // noop
    }

    @Override
    public int getContentSize() {
        return console.getContentSize();
    }

    @Override
    public AnAction @NotNull [] createConsoleActions() {
        return AnAction.EMPTY_ARRAY;
    }

    public AnAction @NotNull [] consoleActions() {
        List<AnAction> actions = new ArrayList<>();
        actions.add(new ToggleUseSoftWrapsToolbarAction(SoftWrapAppliancePlaces.CONSOLE) {
            @Override
            protected @Nullable Editor getEditor(@NotNull AnActionEvent e) {
                return console.getEditor();
            }
        });
        actions.add(new ScrollToTheEndToolbarAction(console.getEditor()));
        actions.add(ActionManager.getInstance().getAction("Print"));
        actions.add(new ClearConsoleAction() {
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(getContentSize() > 0);
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                clear();
            }
        });
        return actions.toArray(AnAction.EMPTY_ARRAY);
    }

    @Override
    public void allowHeavyFilters() {
        // noop
    }

    @Override
    public @Nullable @NonNls String getHelpId() {
        return "reference.runToolWindow.testResultsTab";
    }

    @Override
    public @NotNull JComponent getComponent() {
        return resultPanel;
    }

    @Override
    public JComponent getPreferredFocusableComponent() {
        return resultPanel;
    }

    @Override
    public void dispose() {
        state = state.dispose();
        console = null;
    }
}
