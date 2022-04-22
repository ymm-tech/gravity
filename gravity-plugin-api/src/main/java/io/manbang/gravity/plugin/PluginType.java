package io.manbang.gravity.plugin;

/**
 * 插件类型
 *
 * @author duoliang.zhang
 * @since 2020/08/11 09:31:02
 */
public enum PluginType {
    /**
     * 切面
     */
    ADVICE,
    /**
     * 拦截器
     */
    INTERCEPTOR,
    /**
     * 拦截器实例
     */
    INTERCEPTOR_INSTANCE
}
