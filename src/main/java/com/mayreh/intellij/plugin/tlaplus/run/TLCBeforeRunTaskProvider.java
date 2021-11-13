package com.mayreh.intellij.plugin.tlaplus.run;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Paths;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.mayreh.intellij.plugin.tlaplus.run.TLCBeforeRunTaskProvider.TLCBeforeRunTask;

/**
 * Provide BeforeRunTask that generates TLC model checker config before running TLC
 * if not exists.
 * The task will be added to every new {@link TLCRunConfiguration} by default through
 * {@link TLCRunConfigurationProducer}.
 */
public class TLCBeforeRunTaskProvider extends BeforeRunTaskProvider<TLCBeforeRunTask> {
    public static final Key<TLCBeforeRunTask> ID = Key.create("TLC.BeforeRunTask");
    private static final String TLC_CONFIG_TEMPLATE;
    static {
        try (InputStreamReader reader = new InputStreamReader(
                TLCBeforeRunTaskProvider.class
                        .getClassLoader()
                        .getResourceAsStream("com/mayreh/intellij/plugin/tlc_config_template.cfg"))) {
            TLC_CONFIG_TEMPLATE = StreamUtil.readText(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Key<TLCBeforeRunTask> getId() {
        return ID;
    }

    @Override
    public @Nls(capitalization = Capitalization.Title) String getName() {
        return "Generate TLC config";
    }

    @Override
    public @Nullable Icon getIcon() {
        // TODO
        return super.getIcon();
    }

    @Override
    public @Nullable TLCBeforeRunTask createTask(@NotNull RunConfiguration runConfiguration) {
        return new TLCBeforeRunTask();
    }

    @Override
    public boolean executeTask(@NotNull DataContext context, @NotNull RunConfiguration configuration,
                               @NotNull ExecutionEnvironment environment, @NotNull TLCBeforeRunTask task) {
        if (configuration instanceof TLCRunConfiguration) {
            String modulePath = ((TLCRunConfiguration) configuration).getFile();
            if (StringUtil.isNotEmpty(modulePath)) {
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        VirtualFile moduleFile = VirtualFileManager
                                .getInstance().findFileByNioPath(Paths.get(modulePath));
                        if (moduleFile == null || moduleFile.getParent() == null) {
                            return;
                        }
                        String configFile = moduleFile.getNameWithoutExtension() + ".cfg";
                        if (moduleFile.getParent().findChild(configFile) != null) {
                            return;
                        }
                        try {
                            VirtualFile file = moduleFile.getParent().createChildData(task, configFile);
                            VfsUtil.saveText(file, TLC_CONFIG_TEMPLATE);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                });
            }
        }
        return true;
    }

    public static class TLCBeforeRunTask extends BeforeRunTask<TLCBeforeRunTask> {
        public TLCBeforeRunTask() {
            super(ID);
            setEnabled(true);
        }
    }
}
