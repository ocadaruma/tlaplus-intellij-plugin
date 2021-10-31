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
import com.mayreh.intellij.plugin.tlaplus.run.TLCRunConfiguration;

public class TLCSettingsEditor extends SettingsEditor<TLCRunConfiguration> {
    private JPanel panel;
    private LabeledComponent<TextFieldWithBrowseButton> workingDirectoryField;
    private LabeledComponent<TextFieldWithBrowseButton> fileField;
    private LabeledComponent<TextFieldWithBrowseButton> configFileField;

    @Override
    protected void resetEditorFrom(@NotNull TLCRunConfiguration s) {
        workingDirectoryField.getComponent().setText(s.getWorkingDirectory());
        fileField.getComponent().setText(s.getFile());
        configFileField.getComponent().setText(s.getConfigFile());
    }

    @Override
    protected void applyEditorTo(@NotNull TLCRunConfiguration s) throws ConfigurationException {
        s.setWorkingDirectory(workingDirectoryField.getComponent().getText());
        s.setFile(fileField.getComponent().getText());
        s.setConfigFile(configFileField.getComponent().getText());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

    private void createUIComponents() {
        workingDirectoryField = new LabeledComponent<>();
        fileField = new LabeledComponent<>();
        configFileField = new LabeledComponent<>();

        workingDirectoryField.setComponent(fileChooseComponent(
                FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        fileField.setComponent(fileChooseComponent(
                FileChooserDescriptorFactory.createSingleFileDescriptor("tla")));
        configFileField.setComponent(fileChooseComponent(
                FileChooserDescriptorFactory.createSingleFileDescriptor("cfg")));
    }

    private static TextFieldWithBrowseButton fileChooseComponent(FileChooserDescriptor chooser) {
        return new TextFieldWithBrowseButton() {
            {
                addBrowseFolderListener(null, null, null, chooser);
            }
        };
    }
}
