package io.manbang.gravity.plugin;

/**
 * 总是起作用的开关
 *
 * @author duoliang.zhang
 * @since 2020/11/5 9:51
 */
enum AlwaysWorkingSwitcher implements Switcher {
    INSTANCE;

    @Override
    public void addListener(SwitcherListener listener) {
        listener.on();
    }
}
