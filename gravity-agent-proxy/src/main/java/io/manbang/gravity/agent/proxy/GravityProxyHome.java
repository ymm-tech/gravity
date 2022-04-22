package io.manbang.gravity.agent.proxy;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * 重力proxy目录
 *
 * @author duoliang.zhang
 * @since 2020/9/1 9:13
 */
public enum GravityProxyHome {
    /**
     * instance
     */
    INSTANCE;

    private static final String HOME_DIR_NAME = ".gravity";
    private static final String JAR_FILE_EXTENSION = ".jar";
    private final Logger log;
    private final Path home;

    GravityProxyHome() {
        log = Logger.getLogger(GravityProxyHome.class.getName());
        home = createHomeIfNecessary();
        createAgentDir(home);
    }

    public Path getHome() {
        return home;
    }

    public Path resolvePath(String path) {
        return home.resolve(path);
    }

    private Path createHomeIfNecessary() {
        Path path = Paths.get(System.getProperty("user.home"), HOME_DIR_NAME);
        File file = path.toFile();

        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (created) {
                log.info(() -> String.format("创建重力家目录成功：%s", path));
            }
        }

        return path;
    }

    private void createAgentDir(Path home) {
        File file = home.resolve("base").toFile();

        if (file.exists()) {
            if (AgentProxyOptions.INSTANCE.isLocalDebug()) {
                return;
            }

            Optional.ofNullable(file.listFiles(this::isJarFile))
                    .map(Stream::of)
                    .orElse(Stream.empty())
                    .forEach(File::delete);

        } else {
            boolean created = file.mkdirs();

            if (created) {
                log.info(() -> String.format("创建Agent包目录成功：%s", file));
            }
        }
    }

    private boolean isJarFile(File file) {
        return file.getName().toLowerCase().endsWith(JAR_FILE_EXTENSION);
    }
}
