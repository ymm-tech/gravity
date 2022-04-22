package io.manbang.gravity.plugin;

/**
 * 精灵服务，除了插件机制之外，重力系统还支持后台服务，SPI方式自定义
 *
 * @author dzhang
 */
public interface GravityService extends Ordered, Named {
    /**
     * 服务准备，默认什么都不干，直接返回<code>true</code>，然后继续执行 start 方法，此接口，不允许抛出异常
     *
     * @param classLoader 当前应用的类加载器
     * @return 如果，继续执行 start 接口，则返回<code>true</code>，否则，精灵服务将不会执行
     */
    default boolean prepare(ClassLoader classLoader) {
        return true;
    }

    /**
     * 启动服务
     */
    void start();

    /**
     * 停止服务，如果需要清理资源，请覆盖此接口，默认什么都不干
     */
    default void stop() {
        //
    }
}
