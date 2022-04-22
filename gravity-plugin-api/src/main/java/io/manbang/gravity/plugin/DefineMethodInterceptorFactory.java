package io.manbang.gravity.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilong.hu
 * @since 2022/01/22 14:34
 */

class DefineMethodInterceptorFactory implements InterceptorFactory {
    static final InterceptorFactory INSTANCE = new DefineMethodInterceptorFactory();
    static final Map<Object, DefineMethodInterceptorTemplate> TEMPLATES = new ConcurrentHashMap<>();

    private DefineMethodInterceptorFactory() {
    }

    @Override
    public Object create(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        return TEMPLATES.computeIfAbsent(interceptorClass, c -> new DefineMethodInterceptorTemplate(interceptorClass, extendedExecutor));
    }

    @Override
    public Object create(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        return TEMPLATES.computeIfAbsent(interceptor, k -> new DefineMethodInterceptorTemplate(interceptor, extendedExecutor));
    }
}
