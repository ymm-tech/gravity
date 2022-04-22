package io.manbang.gravity.plugin;

/**
 * @author weilong.hu
 * @since 2022/01/22 14:08
 */
public interface InterceptorFactory {
    /**
     * 拦截方法 支持静态和实例方法
     *
     * @param interceptor      拦截器
     * @param extendedExecutor 扩展执行器
     * @return 拦截模板实例
     */
    static Object method(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        return MethodInterceptorFactory.INSTANCE.create(interceptor, extendedExecutor);
    }

    /**
     * 拦截方法 支持静态和实例方法
     *
     * @param interceptorClass 拦截器class
     * @param extendedExecutor 扩展执行器
     * @return 拦截模板实例
     */
    static Object method(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        return MethodInterceptorFactory.INSTANCE.create(interceptorClass, extendedExecutor);
    }

    /**
     * 拦截构造器
     *
     * @param interceptor      拦截器
     * @param extendedExecutor 扩展执行器
     * @return 拦截模板实例
     */
    static Object constructor(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        return ConstructorInterceptorFactory.INSTANCE.create(interceptor, extendedExecutor);
    }

    /**
     * 拦截构造器
     *
     * @param interceptorClass 拦截器class
     * @param extendedExecutor 扩展执行器
     * @return 拦截模板实例
     */
    static Object constructor(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        return ConstructorInterceptorFactory.INSTANCE.create(interceptorClass, extendedExecutor);
    }

    /**
     * 增加新方法
     *
     * @param interceptor      拦截器
     * @param extendedExecutor 扩展执行器
     * @return 拦截模板实例
     */
    static Object defineMethod(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        return DefineMethodInterceptorFactory.INSTANCE.create(interceptor, extendedExecutor);
    }

    /**
     * 增加新方法
     *
     * @param interceptorClass 拦截器class
     * @param extendedExecutor 扩展执行器
     * @return 拦截模板实例
     */
    static Object defineMethod(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        return DefineMethodInterceptorFactory.INSTANCE.create(interceptorClass, extendedExecutor);
    }


    Object create(Class<?> interceptorClass, ExtendedExecutor extendedExecutor);

    Object create(Interceptor interceptor, ExtendedExecutor extendedExecutor);
}
