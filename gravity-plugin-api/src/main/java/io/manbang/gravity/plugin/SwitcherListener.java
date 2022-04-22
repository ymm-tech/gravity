package io.manbang.gravity.plugin;

/**
 * 开关监听器
 *
 * @author duoliang.zhang
 * @since 2020/11/5 14:07
 */
public interface SwitcherListener extends Named {
    /**
     * 打开开关回调
     */
    void on();

    /**
     * 关闭开关回调
     */
    void off();
}
