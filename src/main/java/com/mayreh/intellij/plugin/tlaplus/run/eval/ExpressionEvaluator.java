package com.mayreh.intellij.plugin.tlaplus.run.eval;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;

import lombok.Value;
import lombok.experimental.Accessors;
import tla2sany.semantic.ModuleNode;
import tla2sany.semantic.OpDefNode;
import tlc2.tool.EvalException;
import tlc2.tool.impl.FastTool;
import tlc2.tool.impl.Tool;
import util.Assert.TLCRuntimeException;
import util.ToolIO;

/**
 * Provides a feature to evaluate constant expression in given module context
 */
public class ExpressionEvaluator {
    private static final Logger LOG = Logger.getInstance(ExpressionEvaluator.class);

    /**
     * Represents the result of evaluation.
     */
    @Value
    @Accessors(fluent = true)
    public static class Result {
        String output;
        List<String> errors;
    }

    /**
     * The context that expression evaluation to be executed.
     * This module will be imported into dummy module for evaluation,
     * so it has to be valid.
     * Otherwise, the evaluation will fail.
     */
    @Value
    @Accessors(fluent = true)
    public static class Context {
        String moduleName;
        Path directory;
    }

    public static Result evaluate(@Nullable Context context,
                                  String expression) {
        String clazzName = ExpressionEvaluator.class.getName();
        IsolatedClassLoader loader = new IsolatedClassLoader(
                ExpressionEvaluator.class.getClassLoader());
        try {
            Class<?> clazz = Class.forName(clazzName, true, loader);
            Method method = clazz.getDeclaredMethod("evaluateInternal", Context.class, String.class);
            return (Result) method.invoke(null, context, expression);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // called by reflection
    private static Result evaluateInternal(
            @Nullable Context context,
            String expression) {
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("tla");
            Path dummyModuleFile = tmpDir.resolve(DummyModule.moduleName() + ".tla");
            Path dummyCfgFile = tmpDir.resolve(DummyModule.moduleName() + ".cfg");

            FileUtil.writeToFile(dummyModuleFile.toFile(), createDummyModule(context, expression));
            FileUtil.writeToFile(dummyCfgFile.toFile(), createDummyConfig());

            final Path moduleDirectory;
            if (context != null) {
                moduleDirectory = context.directory;
            } else {
                moduleDirectory = tmpDir;
            }

            // To collect error messages instead of writing to stdout
            ToolIO.setMode(ToolIO.TOOL);
            ToolIO.reset();
            Runner runner = new Runner(new FilenameResolver(moduleDirectory, new DummyModule(dummyModuleFile)));
            tlc2.module.TLC.OUTPUT = runner;

            return runner.run("replvalue");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            tlc2.module.TLC.OUTPUT = null;
            if (tmpDir != null) {
                delete(tmpDir);
            }
        }
    }

    private static String createDummyModule(@Nullable Context context, String expression) {
        StringBuilder moduleBuilder = new StringBuilder();
        moduleBuilder
                .append("---- MODULE ").append(DummyModule.moduleName()).append(" ----").append('\n')
                .append("EXTENDS Reals,Sequences,Bags,FiniteSets,TLC,Randomization");
        if (context != null) {
            moduleBuilder.append(',' + context.moduleName);
        }
        moduleBuilder.append('\n');
        moduleBuilder
                .append("VARIABLE replvar").append('\n')
                .append("replinit == replvar = 0").append('\n')
                .append("replnext == replvar' = 0").append('\n')
                .append("replvalue == ").append(expression).append('\n');
        moduleBuilder.append("====");

        return moduleBuilder.toString();
    }

    private static String createDummyConfig() {
        StringBuilder cfgBuilder = new StringBuilder();
        cfgBuilder
                .append("INIT replinit").append('\n')
                .append("NEXT replnext");
        return cfgBuilder.toString();
    }

    private static void delete(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }
            });
            Files.deleteIfExists(dir);
        } catch (IOException e) {
            LOG.warn("Failed to clean up directory", e);
        }
    }

    private static class Runner extends Writer {
        private final FilenameResolver resolver;
        private final StringWriter delegate;

        Runner(FilenameResolver resolver) {
            this.resolver = resolver;
            delegate = new StringWriter();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            delegate.write(cbuf, off, len);
        }

        @Override
        public void flush() {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        Result run(String opName) {
            StringBuilder output = new StringBuilder();
            List<String> errors = new ArrayList<>();
            try {
                Tool tool = new FastTool(DummyModule.moduleName(), DummyModule.moduleName(), resolver);
                ModuleNode module = tool.getSpecProcessor().getRootModule();
                OpDefNode valueNode = module.getOpDef(opName);
                output.append(((tlc2.value.impl.Value) tool.eval(valueNode.getBody())).toString());
            } catch (EvalException e) {
                errors.add(e.getMessage());
            } catch (TLCRuntimeException e) {
                errors.add(e.getMessage());
                if (e.parameters != null) {
                    errors.addAll(Arrays.asList(e.parameters));
                }
            } finally {
                flush();
                output.append(delegate);
            }

            return new Result(output.toString(), errors);
        }
    }

    private static class IsolatedClassLoader extends URLClassLoader {
        private final ClassLoader delegate;

        private final Map<String, Class<?>> cache = new HashMap<>();
        private final Set<String> packages = Set.of(
                "tla2sany", "pcal", "util", "tla2tex", "tlc2");

        IsolatedClassLoader(ClassLoader current) {
            super(new URL[]{toolsJarURL()});
            delegate = current;
        }

        private static URL toolsJarURL() {
            try {
                return PathManager.getJarForClass(tlc2.TLC.class).toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (cache.containsKey(name)) {
                return cache.get(name);
            }
            for (String pkg : packages) {
                if (name.startsWith(pkg)) {
                    Class<?> clazz = findClass(name);
                    cache.put(name, clazz);
                    return clazz;
                }
            }
            return delegate.loadClass(name);
        }
    }
}
