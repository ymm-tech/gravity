package io.manbang.gravity.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilong.hu
 * @since 2022/01/22 14:27
 */
class ConstructorInterceptorFactory implements InterceptorFactory {
    static final InterceptorFactory INSTANCE = new ConstructorInterceptorFactory();
    private static final Map<Object, ConstructorInterceptorTemplate> TEMPLATES = new ConcurrentHashMap<>();

    private ConstructorInterceptorFactory() {
    }

    @Override
    public Object create(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        return TEMPLATES.computeIfAbsent(interceptorClass, c -> new ConstructorInterceptorTemplate(interceptorClass, extendedExecutor));
    }

    @Override
    public Object create(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        return TEMPLATES.computeIfAbsent(interceptor, k -> new ConstructorInterceptorTemplate(interceptor, extendedExecutor));
    }
}
