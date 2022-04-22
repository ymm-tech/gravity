package io.manbang.gravity.plugin;

import java.util.List;

/**
 * 插件加载器
 *
 * @author duoliang.zhang
 * @since 2020/9/10 16:10
 */
public interface PluginLoader extends Ordered, Named {
    /**
     * 获取一个插件载入器，用来载入插
     *
     * @return 插件载入器
     */
    static PluginLoader loader() {
        return Services.loadFirst(PluginLoader.class, LocalPluginLoader.INSTANCE);
    }

    /**
     * 载入插件列表，只是列表清单，没有下载到本地
     *
     * @param options Agent命令行参数
     * @return 插件Jar包列表
     */
    List<PluginJar> loadPluginJars(AgentOptions options);
}
