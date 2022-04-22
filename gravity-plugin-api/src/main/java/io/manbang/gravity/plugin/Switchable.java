package io.manbang.gravity.plugin;

/**
 * 标记插件是否支持在线开关控制，如果支持开关控制的话，会被包装成一个可开关控制的插件
 *
 * @author duoliang.zhang
 * @since 2020/11/26 16:56
 */
public interface Switchable {

    /**
     * 判断
     *
     * @return 如果当前切面支持开关控制，返回<code>true</code>
     */
    default boolean isSwitchable() {
        return true;
    }
}
