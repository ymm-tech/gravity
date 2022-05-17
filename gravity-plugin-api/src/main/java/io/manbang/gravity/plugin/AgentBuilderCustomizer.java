package io.manbang.gravity.plugin;


import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * 代理构建器定制器SPI，用户可以自定义，如果没有自定义需求，则利用默认定制器 {@link DefaultAgentBuilderCustomizer}
 *
 * @author 章多亮
 * @since 2020-08-07
 */
public interface AgentBuilderCustomizer {
    /**
     * 当没有用户自定义的定制器时，用默认的定制器
     *
     * @param pluginClassLoader 插件加载器
     * @return 代理构建定制器
     */
    static AgentBuilderCustomizer customizer(AgentPluginClassLoader pluginClassLoader) {
        return Services.loadFirst(AgentBuilderCustomizer.class, pluginClassLoader, DefaultAgentBuilderCustomizer.INSTANCE);
    }

    /**
     * 用户自定义代理构建器
     *
     * @param options Agent入参
     * @param builder 代理构建器
     * @return 配置后的代理构建器
     */
    AgentBuilder customize(final AgentOptions options, final AgentBuilder builder);
}
