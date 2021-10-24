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
    private JPanel myPanel;
    private LabeledComponent<TextFieldWithBrowseButton> myWorkingDirectory;
    private LabeledComponent<TextFieldWithBrowseButton> myFile;
    private LabeledComponent<TextFieldWithBrowseButton> myConfigFile;

    @Override
    protected void resetEditorFrom(@NotNull TLCRunConfiguration s) {
        myWorkingDirectory.getComponent().setText(s.getWorkingDirectory());
        myFile.getComponent().setText(s.getFile());
        myConfigFile.getComponent().setText(s.getConfigFile());
    }

    @Override
    protected void applyEditorTo(@NotNull TLCRunConfiguration s) throws ConfigurationException {
        s.setWorkingDirectory(myWorkingDirectory.getComponent().getText());
        s.setFile(myFile.getComponent().getText());
        s.setConfigFile(myConfigFile.getComponent().getText());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return myPanel;
    }

    private void createUIComponents() {
        myWorkingDirectory = new LabeledComponent<>();
        myFile = new LabeledComponent<>();
        myConfigFile = new LabeledComponent<>();

        myWorkingDirectory.setComponent(fileChooseComponent(
                FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        myFile.setComponent(fileChooseComponent(
                FileChooserDescriptorFactory.createSingleFileDescriptor("tla")));
        myConfigFile.setComponent(fileChooseComponent(
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
