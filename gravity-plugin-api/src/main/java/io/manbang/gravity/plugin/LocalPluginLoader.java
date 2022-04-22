package io.manbang.gravity.plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author duoliang.zhang
 * @since 2020/9/10 16:40
 */
enum LocalPluginLoader implements PluginLoader {
    INSTANCE;

    @Override
    public List<PluginJar> loadPluginJars(AgentOptions options) {
        return Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
