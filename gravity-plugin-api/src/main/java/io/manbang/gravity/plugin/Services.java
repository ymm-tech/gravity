package io.manbang.gravity.plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * @author duoliang.zhang
 * @since 2020/9/10 17:05
 */
public class Services {
    private Services() {
        throw new UnsupportedOperationException();
    }

    /**
     * 从当前线程类加载起载入所有的服务
     *
     * @param serviceClass SPI接口类
     * @param <T>          服务的具体类型
     * @return 服务列表
     */
    public static <T> List<T> loadAll(Class<T> serviceClass) {
        return loadAll(serviceClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 从指定类加载起载入所有的服务
     *
     * @param serviceClass SPI接口类
     * @param classLoader  类加载器，从指定的类加载加载服务
     * @param <T>          服务的具体类型
     * @return 服务列表
     */
    public static <T> List<T> loadAll(Class<T> serviceClass, ClassLoader classLoader) {
        ServiceLoader<T> services = ServiceLoader.load(serviceClass, classLoader);

        List<T> list = new ArrayList<>();

        for (T service : services) {
            list.add(service);
        }

        if (Ordered.class.isAssignableFrom(serviceClass)) {
            list.sort(Comparator.comparingInt(o -> ((Ordered) o).getOrder()));
        }

        return list;
    }

    /**
     * 从当前线程类加载起载入第一个加载的服务，如果服务实现 {@link Ordered} 接口，则先排序，后返回，否则，直接返回第一个加载的服务，顺序取决于JDK SPI本身
     *
     * @param serviceClass SPI接口类
     * @param <T>          服务的具体类型
     * @return 服务实例
     */
    public static <T> Optional<T> loadFirst(Class<T> serviceClass) {
        return Optional.ofNullable(loadFirst(serviceClass, null));
    }

    /**
     * 从当前线程类加载起载入第一个加载的服务，如果服务实现 {@link Ordered} 接口，则先排序，后返回，否则，直接返回第一个加载的服务，顺序取决于JDK SPI本身
     *
     * @param serviceClass   SPI接口类
     * @param defaultService 当什么服务都没有找到的时候，返回这个默认服务
     * @param <T>            服务的具体类型
     * @return 服务实例
     */
    public static <T> T loadFirst(Class<T> serviceClass, T defaultService) {
        return loadFirst(serviceClass, Thread.currentThread().getContextClassLoader(), defaultService);
    }

    /**
     * 从指定类加载器载入第一个加载的服务，如果服务实现 {@link Ordered} 接口，则先排序，后返回，否则，直接返回第一个加载的服务，顺序取决于JDK SPI本身
     *
     * @param serviceClass   SPI接口类
     * @param classLoader    类加载器，从指定的类加载加载服务
     * @param defaultService 当什么服务都没有找到的时候，返回这个默认服务
     * @param <T>            服务的具体类型
     * @return 服务实例
     */
    public static <T> T loadFirst(Class<T> serviceClass, ClassLoader classLoader, T defaultService) {
        List<T> list = loadAll(serviceClass, classLoader);

        if (list.isEmpty()) {
            return defaultService;
        }

        return list.get(0);
    }
}
