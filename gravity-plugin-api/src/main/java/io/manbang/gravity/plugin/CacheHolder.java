package io.manbang.gravity.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 开关缓存容器
 *
 * @author duoliang.zhang
 * @since 2020/11/5 10:02
 */
enum CacheHolder {
    /**
     * instance
     */
    INSTANCE;
    private static final Map<Class<?>, Map<ClassLoader, Object>> HOLDER = new ConcurrentHashMap<>();

    /**
     * 载入开关服务，如果缓存中，不存在，则从SPI中加载实现
     *
     * @param classLoader 开关SPI所在的类加载器
     * @return 开关
     */
    @SuppressWarnings("unchecked")
    public <T> T loadIfAbsent(ClassLoader classLoader, Class<T> clazz, T defaultValue) {
        final Map<ClassLoader, Object> cacheMap = HOLDER.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
        return (T) cacheMap.computeIfAbsent(classLoader, cl -> Services.loadFirst(clazz, cl, defaultValue));
    }
}
