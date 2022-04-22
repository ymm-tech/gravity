package io.manbang.gravity.plugin;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * 重力系统家目录，存放依赖的Jar或者配置文件，枚举类型单例
 *
 * @author duoliang.zhang
 * @since 2020/9/1 9:13
 */
public enum GravityHome {
    INSTANCE;

    private static final String HOME_DIR_NAME = ".gravity";
    private static final String JAR_FILE_EXTENSION = ".jar";
    private final Logger log;
    private final Path home;

    GravityHome() {
        log = Logger.getLogger(GravityHome.class.getName());
        home = createHomeIfNecessary();
        createJarDirectories(home);
    }

    public Path getHome() {
        return home;
    }

    public File toFile(JarType jarType) {
        return resolvePath(jarType).toFile();
    }

    public URL[] listFileUrls(JarType jarType) {
        return Stream.of(listFiles(jarType))
                .map(File::toURI)
                .map(GravityUtils::toUrl)
                .filter(Objects::nonNull)
                .toArray(URL[]::new);
    }

    public File[] listFiles(JarType jarType) {
        File file = home.resolve(jarType.name().toLowerCase()).toFile();
        if (file.exists()) {
            File[] jars = file.listFiles(pathname -> pathname.getName().endsWith(JAR_FILE_EXTENSION));
            return jars == null ? new File[0] : jars;
        }

        return new File[0];
    }

    public String[] listFileNames(JarType jarType) {
        File file = home.resolve(jarType.name().toLowerCase()).toFile();
        if (file.exists()) {
            String[] jarNames = file.list((dir, name) -> name.endsWith(JAR_FILE_EXTENSION));
            return jarNames == null ? new String[0] : jarNames;
        }

        return new String[0];
    }

    public Path resolvePath(JarType type) {
        return resolvePath(type.name().toLowerCase());
    }

    public Path resolvePath(String path) {
        return home.resolve(path);
    }

    private Path createHomeIfNecessary() {
        Path path = Paths.get(System.getProperty("user.home"), HOME_DIR_NAME, AgentOptions.INSTANCE.getAppName());
        File file = path.toFile();

        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (created) {
                log.info(() -> String.format("创建重力家目录成功：%s", path));
            }
        }

        return path;
    }

    private void createJarDirectories(Path home) {
        for (JarType jarType : JarType.values()) {
            File file = home.resolve(jarType.name().toLowerCase()).toFile();

            if (file.exists()) {
                if (AgentOptions.INSTANCE.isLocalDebug()) {
                    return;
                }

                Optional.ofNullable(file.listFiles(this::isJarFile))
                        .map(Stream::of)
                        .orElse(Stream.empty())
                        .forEach(File::delete);

            } else {
                boolean created = file.mkdirs();

                if (created) {
                    log.info(() -> String.format("创建Jar包目录成功：%s", file));
                }
            }
        }
    }

    private boolean isJarFile(File file) {
        return file.getName().toLowerCase().endsWith(JAR_FILE_EXTENSION);
    }
}
