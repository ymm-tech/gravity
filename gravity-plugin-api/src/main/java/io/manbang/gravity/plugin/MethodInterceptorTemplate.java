package io.manbang.gravity.plugin;


import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

import static io.manbang.gravity.plugin.ReflectUtil.newInstance;

/**
 * @author duoliang.zhang
 * @since 2020/8/26 9:35
 */
public class MethodInterceptorTemplate {

    private final Interceptor interceptor;
    private final ExtendedExecutor extendedExecutor;

    MethodInterceptorTemplate(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        this.interceptor = Interceptor.switcher(newInstance(interceptorClass));
        this.extendedExecutor = extendedExecutor;
    }

    MethodInterceptorTemplate(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        this.interceptor = Interceptor.switcher(interceptor);
        this.extendedExecutor = extendedExecutor;
    }

    @RuntimeType
    public Object intercept(@Origin Class<?> targetClass,
                            @This(optional = true) Object target,
                            @Origin Method method,
                            @AllArguments Object[] arguments,
                            @Morph MorphingCallable<?> callable) throws Throwable {

        ExecuteContext context = ExecuteContext.builder()
                .targetClass(targetClass)
                .target(target)
                .method(method)
                .arguments(arguments)
                .extraExecutor(extendedExecutor)
                .build();

        boolean continued = interceptor.beforeMethod(context);

        if (continued) {
            try {
                Object result = callable.call(context.getArguments());
                context.setResult(result);
                interceptor.afterMethod(context);
            } catch (Throwable e) {
                context.setThrowable(e);
                interceptor.handleException(context);
            } finally {
                interceptor.onCompleted(context);
            }
        }

        Throwable throwable = context.getThrowable();
        if (throwable == null) {
            return context.getResult();
        } else {
            throw throwable;
        }
    }
}
