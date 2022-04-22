package io.manbang.gravity.plugin;

/**
 * 生命周期
 *
 * @author duoliang.zhang
 * @see Advice 切面
 * @see Interceptor 拦截器
 * @since 2020/11/25 16:05
 */
public interface LifeCycle {
    /**
     * 插件启用的时候，会回调此方法
     */
    default void resume() {
        // do nothing
    }

    /**
     * 插件禁用的时候，会回调方法
     */
    default void suspend() {
        // do nothing
    }
}
