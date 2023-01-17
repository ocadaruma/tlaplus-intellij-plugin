package com.mayreh.intellij.plugin.tlaplus.run.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import com.mayreh.intellij.plugin.tlaplus.run.TLCRunConfiguration;

public class TLCSettingsEditor extends SettingsEditor<TLCRunConfiguration> {
    private JPanel panel;
    private LabeledComponent<TextFieldWithBrowseButton> workingDirectoryField;
    private LabeledComponent<TextFieldWithBrowseButton> fileField;
    private LabeledComponent<RawCommandLineEditor> tlcArgumentsEditor;
    private LabeledComponent<RawCommandLineEditor> jvmOptionsEditor;

    @Override
    protected void resetEditorFrom(@NotNull TLCRunConfiguration s) {
        workingDirectoryField.getComponent().setText(s.getWorkingDirectory());
        fileField.getComponent().setText(s.getFile());
        tlcArgumentsEditor.getComponent().setText(s.getTlcArguments());
        jvmOptionsEditor.getComponent().setText(s.getJvmOptions());
    }

    @Override
    protected void applyEditorTo(@NotNull TLCRunConfiguration s) throws ConfigurationException {
        s.setWorkingDirectory(workingDirectoryField.getComponent().getText());
        s.setFile(fileField.getComponent().getText());
        s.setTlcArguments(tlcArgumentsEditor.getComponent().getText());
        s.setJvmOptions(jvmOptionsEditor.getComponent().getText());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

    private void createUIComponents() {
        workingDirectoryField = new LabeledComponent<>();
        fileField = new LabeledComponent<>();
        tlcArgumentsEditor = new LabeledComponent<>();
        jvmOptionsEditor = new LabeledComponent<>();

        workingDirectoryField.setComponent(fileChooseComponent(
                FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        fileField.setComponent(fileChooseComponent(
                FileChooserDescriptorFactory.createSingleFileDescriptor("tla")));
        tlcArgumentsEditor.setComponent(new RawCommandLineEditor());
        jvmOptionsEditor.setComponent(new RawCommandLineEditor());
    }

    private static TextFieldWithBrowseButton fileChooseComponent(FileChooserDescriptor chooser) {
        return new TextFieldWithBrowseButton() {
            {
                addBrowseFolderListener(null, null, null, chooser);
            }
        };
    }
}
