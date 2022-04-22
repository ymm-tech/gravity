package io.manbang.gravity.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilong.hu
 * @since 2022/01/22 14:11
 */
class MethodInterceptorFactory implements InterceptorFactory {
    static final InterceptorFactory INSTANCE = new MethodInterceptorFactory();
    private static final Map<Object, MethodInterceptorTemplate> TEMPLATES = new ConcurrentHashMap<>();

    private MethodInterceptorFactory() {
    }

    @Override
    public Object create(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        return TEMPLATES.computeIfAbsent(interceptorClass, c -> new MethodInterceptorTemplate(interceptorClass, extendedExecutor));
    }

    @Override
    public Object create(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        return TEMPLATES.computeIfAbsent(interceptor, k -> new MethodInterceptorTemplate(interceptor, extendedExecutor));
    }
}
