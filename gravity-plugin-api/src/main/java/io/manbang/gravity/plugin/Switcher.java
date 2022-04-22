package io.manbang.gravity.plugin;

import java.util.EventListener;

/**
 * 插件使能开关
 *
 * @author duoliang.zhang
 * @since 2020/10/28 18:54
 */
public interface Switcher extends EventListener {
    /**
     * 从SPI中获取一个控制开关，用于控制左右的插件是否生效，如果没有提供开关的实现，则直接返回默认开关实现，总是工作的开关
     *
     * @return 开关
     */
    static Switcher of(ClassLoader classLoader) {
        return CacheHolder.INSTANCE.loadIfAbsent(classLoader, Switcher.class, AlwaysWorkingSwitcher.INSTANCE);
    }

    /**
     * 监听指定开关量变化
     *
     * @param listener 开关监听器
     */
    default void addListener(SwitcherListener listener) {
        // do nothing
    }
}
