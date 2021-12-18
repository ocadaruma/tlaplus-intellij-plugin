package com.mayreh.intellij.plugin.tlaplus.run.eval;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import util.FilenameToStream;
import util.SimpleFilenameToStream;

/**
 * A {@link FilenameToStream} impl that resolves dummy module to dummy directory and
 * otherwise just delegates to {@link SimpleFilenameToStream}.
 *
 * This is to apply different module name resolution logic.
 * Expression evaluation on specific module works like below:
 * - Create dummy module that extends the module in temporary directory
 * - Run TLC against dummy module
 * Hence, in normal resolution logic, dummy module and the imported module must be in same directory.
 * We don't want to create dummy module in module's directory, so we need custom resolver.
 */
public class FilenameResolver implements FilenameToStream {
    private final SimpleFilenameToStream delegate;
    private final DummyModule dummyModule;

    public FilenameResolver(Path moduleDirectory,
                            DummyModule dummyModule) {
        delegate = new SimpleFilenameToStream(moduleDirectory.toAbsolutePath().toString());
        this.dummyModule = dummyModule;
    }

    @Override
    public File resolve(String name, boolean isModule) {
        final String nameWithoutExtension;
        if (name.contains(".")) {
            nameWithoutExtension = name.substring(0, name.lastIndexOf('.'));
        } else {
            nameWithoutExtension = name;
        }
        if (Objects.equals(nameWithoutExtension, DummyModule.moduleName())) {
            return dummyModule.moduleFile()
                              .getParent()
                              .resolve(name)
                              .toFile();
        }
        return delegate.resolve(name, isModule);
    }

    @Override
    public String getFullPath() {
        return delegate.getFullPath();
    }

    @Override
    public boolean isStandardModule(String moduleName) {
        return delegate.isStandardModule(moduleName);
    }
}
