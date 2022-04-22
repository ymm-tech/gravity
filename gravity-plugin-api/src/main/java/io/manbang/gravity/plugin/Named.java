package io.manbang.gravity.plugin;

/**
 * 具名接口
 *
 * @author duoliang.zhang
 * @since 2020/9/10 19:39
 */
public interface Named {

    /**
     * 获取插件加载器名称，默认是类的简单名
     *
     * @return 名称
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
