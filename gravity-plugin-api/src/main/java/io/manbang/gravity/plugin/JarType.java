package io.manbang.gravity.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Jar包类型，不同类型Jar会被放到不同的目录中
 *
 * @author duoliang.zhang
 * @since 2020/8/12 13:46
 */
public enum JarType {
    /**
     * boot class
     */
    BOOT {
        @Override
        public void appendClassPath(Instrumentation instrumentation, Path gravityHome) {
            doAppendClassPath(gravityHome.resolve(name().toLowerCase()).toFile(), instrumentation::appendToBootstrapClassLoaderSearch);
        }
    },
    /**
     * application class
     */
    APP {
        @Override
        public void appendClassPath(Instrumentation instrumentation, Path gravityHome) {
            doAppendClassPath(gravityHome.resolve(name().toLowerCase()).toFile(), instrumentation::appendToSystemClassLoaderSearch);
        }
    },
    /**
     * agent class
     */
    AGENT,
    /**
     * Tomcat WEB-INF/lib
     */
    TOMCAT;

    private static JarFile newJarFile(File file) {
        try {
            return new JarFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void doAppendClassPath(File classpath, Consumer<JarFile> appendConsumer) {
        Optional.ofNullable(classpath.listFiles())
                .map(Stream::of)
                .orElse(Stream.empty())
                .filter(file -> file.canRead() && file.getName().endsWith(".jar"))
                .map(JarType::newJarFile)
                .filter(Objects::nonNull)
                .forEach(appendConsumer);
    }

    public void appendClassPath(Instrumentation instrumentation, Path gravityHome) {
        // do nothing
    }
}
