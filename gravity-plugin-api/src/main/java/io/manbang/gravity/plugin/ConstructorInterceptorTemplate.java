package io.manbang.gravity.plugin;

import io.manbang.gravity.bytebuddy.implementation.bind.annotation.AllArguments;
import io.manbang.gravity.bytebuddy.implementation.bind.annotation.Origin;
import io.manbang.gravity.bytebuddy.implementation.bind.annotation.RuntimeType;
import io.manbang.gravity.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Constructor;

import static io.manbang.gravity.plugin.ReflectUtil.newInstance;

/**
 * @author weilong.hu
 * @since 2021/09/13 10:54
 */
public class ConstructorInterceptorTemplate {
    private final Interceptor interceptor;
    private final ExtendedExecutor extendedExecutor;

    ConstructorInterceptorTemplate(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        this.interceptor = Interceptor.switcher(newInstance(interceptorClass));
        this.extendedExecutor = extendedExecutor;
    }

    ConstructorInterceptorTemplate(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        this.interceptor = Interceptor.switcher(interceptor);
        this.extendedExecutor = extendedExecutor;
    }

    @RuntimeType
    public void intercept(@Origin Class<?> targetClass,
                          @This(optional = true) Object target,
                          @Origin Constructor<?> constructor,
                          @AllArguments Object[] arguments) {

        ExecuteContext context = ExecuteContext.builder()
                .targetClass(targetClass)
                .target(target)
                .constructor(constructor)
                .arguments(arguments)
                .extraExecutor(extendedExecutor)
                .build();

        interceptor.onCompleted(context);
    }
}
