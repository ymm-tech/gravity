package io.manbang.gravity.plugin;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

import static io.manbang.gravity.plugin.ReflectUtil.newInstance;

/**
 * @author weilong.hu
 * @since 2022/01/21 15:43
 */
public class DefineMethodInterceptorTemplate {

    private final Interceptor interceptor;
    private final ExtendedExecutor extendedExecutor;


    DefineMethodInterceptorTemplate(Class<?> interceptorClass, ExtendedExecutor extendedExecutor) {
        this.interceptor = Interceptor.switcher(newInstance(interceptorClass));
        this.extendedExecutor = extendedExecutor;
    }

    DefineMethodInterceptorTemplate(Interceptor interceptor, ExtendedExecutor extendedExecutor) {
        this.interceptor = Interceptor.switcher(interceptor);
        this.extendedExecutor = extendedExecutor;
    }

    @RuntimeType
    public Object intercept(@Origin Class<?> targetClass,
                            @This(optional = true) Object target,
                            @Origin Method method,
                            @AllArguments Object[] arguments) throws Throwable {

        ExecuteContext context = ExecuteContext.builder()
                .targetClass(targetClass)
                .target(target)
                .method(method)
                .arguments(arguments)
                .extraExecutor(extendedExecutor)
                .build();

        interceptor.onCompleted(context);
        Throwable throwable = context.getThrowable();
        if (throwable == null) {
            return context.getResult();
        } else {
            throw throwable;
        }
    }
}
