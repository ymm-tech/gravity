package io.manbang.gravity.plugin;

import io.manbang.gravity.bytebuddy.asm.Advice;
import io.manbang.gravity.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.manbang.gravity.plugin.Constants.ADVICE_LOGGER_NAME;
import static io.manbang.gravity.plugin.Constants.ENTER_METHOD_ERROR;
import static io.manbang.gravity.plugin.Constants.EXIT_METHOD_ERROR;

/**
 * Advice的执行模板，因为如果Advice需要捕捉异常的话，对性能损失比较大，所以分两种情况，一种是不捕捉异常，一种捕捉异常，大家酌情使用
 * 之所以要动态生成模板，是因为Advice方式的拦截，需要静态方法拦截，同时我们又系统将自定义的注入逻辑加到被拦截的方法上，只能通过静态属性，
 * 才能让静态方法访问，如果变成了静态属性，那么就会变成一个共享变量，但不同的Advice实现，肯定是不能冲突的，所以只能通过动态创建Advice模板类型的方式，
 * 让Advice属性不共享，每个Advice单独生成一个模板类，类名是 Advice#fqcn$Template，也即是每个Advice的一个 Template内部类。
 * 相当于每个Advice会变成如下代码，内部多了个 Template 静态内部类：
 * <pre>
 * public class TimingAdvice implements Advice {
 *     public static final class Template {
 *     // Advice Template 中的代码
 *     }
 *
 *     // enterMethod 逻辑
 *     // exitMethod 逻辑
 * }
 * </pre>
 *
 * @author duoliang.zhang
 * @since 2020/8/26 9:36
 */
public class AdviceTemplate {
    public static final String WITH_THROWABLE_TEMPLATE = "io.manbang.gravity.plugin.AdviceTemplate$WithThrowableTemplate";
    public static final String WITH_METHOD_TEMPLATE = "io.manbang.gravity.plugin.AdviceTemplate$WithMethodTemplate";
    public static final String WITH_CONSTRUCTOR_TEMPLATE = "io.manbang.gravity.plugin.AdviceTemplate$WithConstructorTemplate";
    public static final String MINI_TEMPLATE = "io.manbang.gravity.plugin.AdviceTemplate$MiniTemplate";
    public static final String FULL_TEMPLATE = "io.manbang.gravity.plugin.AdviceTemplate$FullTemplate";
    public static final Logger LOGGER = Logger.getLogger(ADVICE_LOGGER_NAME);

    private AdviceTemplate() {
        throw new UnsupportedOperationException();
    }

    public static ExecuteContext getContext(Class<?> targetClass, Object target, Method method, Object[] arguments, Constructor<?> constructor, ExtendedExecutor extendedExecutor) {
        return ExecuteContext.builder()
                .targetClass(targetClass)
                .target(target)
                .method(method)
                .arguments(arguments)
                .constructor(constructor)
                .extraExecutor(extendedExecutor)
                .build();
    }

    public static class MiniTemplate {
        private static io.manbang.gravity.plugin.Advice advice;
        private static ExtendedExecutor extendedExecutor;

        private MiniTemplate() {
            throw new UnsupportedOperationException();
        }

        public static io.manbang.gravity.plugin.Advice getAdvice() {
            return advice;
        }

        public static void setAdvice(io.manbang.gravity.plugin.Advice advice) {
            MiniTemplate.advice = advice;
        }

        public static ExtendedExecutor getExtendedExecutor() {
            return extendedExecutor;
        }

        public static void setExtendedExecutor(ExtendedExecutor extendedExecutor) {
            MiniTemplate.extendedExecutor = extendedExecutor;
        }

        @Advice.OnMethodEnter
        public static ExecuteContext enterMethod(@Advice.Origin Class<?> targetClass,
                                                 @Advice.This(optional = true) Object target,
                                                 @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecuteContext context = getContext(targetClass, target, null, arguments, null, getExtendedExecutor());
            try {
                getAdvice().enterMethod(context);
                arguments = context.getArguments();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, ENTER_METHOD_ERROR, e);
            }
            return context;
        }

        @Advice.OnMethodExit
        public static void exitMethod(@Advice.Enter ExecuteContext context,
                                      @Advice.This(optional = true) Object target,
                                      @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            context.setTarget(target);
            context.setResult(result);

            try {
                getAdvice().exitMethod(context);
                result = context.getResult();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, EXIT_METHOD_ERROR, e);
            }
        }
    }

    public static class WithMethodTemplate {
        private static io.manbang.gravity.plugin.Advice advice;
        private static ExtendedExecutor extendedExecutor;

        private WithMethodTemplate() {
            throw new UnsupportedOperationException();
        }

        public static io.manbang.gravity.plugin.Advice getAdvice() {
            return advice;
        }

        public static void setAdvice(io.manbang.gravity.plugin.Advice advice) {
            WithMethodTemplate.advice = advice;
        }

        public static ExtendedExecutor getExtendedExecutor() {
            return extendedExecutor;
        }

        public static void setExtendedExecutor(ExtendedExecutor extendedExecutor) {
            WithMethodTemplate.extendedExecutor = extendedExecutor;
        }

        @Advice.OnMethodEnter
        public static ExecuteContext enterMethod(@Advice.Origin Class<?> targetClass,
                                                 @Advice.This(optional = true) Object target,
                                                 @Advice.Origin Method method,
                                                 @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecuteContext context = getContext(targetClass, target, method, arguments, null, getExtendedExecutor());
            try {
                getAdvice().enterMethod(context);
                arguments = context.getArguments();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, ENTER_METHOD_ERROR, e);
            }
            return context;
        }

        @Advice.OnMethodExit
        public static void exitMethod(@Advice.Enter ExecuteContext context,
                                      @Advice.This(optional = true) Object target,
                                      @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            context.setTarget(target);
            context.setResult(result);

            try {
                getAdvice().exitMethod(context);
                result = context.getResult();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, EXIT_METHOD_ERROR, e);
            }
        }
    }

    public static class WithConstructorTemplate {
        private static io.manbang.gravity.plugin.Advice advice;
        private static ExtendedExecutor extendedExecutor;

        private WithConstructorTemplate() {
            throw new UnsupportedOperationException();
        }

        public static io.manbang.gravity.plugin.Advice getAdvice() {
            return advice;
        }

        public static void setAdvice(io.manbang.gravity.plugin.Advice advice) {
            WithConstructorTemplate.advice = advice;
        }

        public static ExtendedExecutor getExtendedExecutor() {
            return extendedExecutor;
        }

        public static void setExtendedExecutor(ExtendedExecutor extendedExecutor) {
            WithConstructorTemplate.extendedExecutor = extendedExecutor;
        }

        @Advice.OnMethodEnter
        public static ExecuteContext enterMethod(@Advice.Origin Class<?> targetClass,
                                                 @Advice.This(optional = true) Object target,
                                                 @Advice.Origin Constructor<?> constructor,
                                                 @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecuteContext context = getContext(targetClass, target, null, arguments, constructor, getExtendedExecutor());
            try {
                getAdvice().enterMethod(context);
                arguments = context.getArguments();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, ENTER_METHOD_ERROR, e);
            }
            return context;
        }

        @Advice.OnMethodExit
        public static void exitMethod(@Advice.Enter ExecuteContext context,
                                      @Advice.This(optional = true) Object target,
                                      @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            context.setTarget(target);
            context.setResult(result);

            try {
                getAdvice().exitMethod(context);
                result = context.getResult();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, EXIT_METHOD_ERROR, e);
            }
        }
    }

    public static class WithThrowableTemplate {
        private static io.manbang.gravity.plugin.Advice advice;
        private static ExtendedExecutor extendedExecutor;

        private WithThrowableTemplate() {
            throw new UnsupportedOperationException();
        }

        public static io.manbang.gravity.plugin.Advice getAdvice() {
            return advice;
        }

        public static void setAdvice(io.manbang.gravity.plugin.Advice advice) {
            WithThrowableTemplate.advice = advice;
        }

        public static ExtendedExecutor getExtendedExecutor() {
            return extendedExecutor;
        }

        public static void setExtendedExecutor(ExtendedExecutor extendedExecutor) {
            WithThrowableTemplate.extendedExecutor = extendedExecutor;
        }

        @Advice.OnMethodEnter
        public static ExecuteContext enterMethod(@Advice.Origin Class<?> targetClass,
                                                 @Advice.This(optional = true) Object target,
                                                 @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecuteContext context = getContext(targetClass, target, null, arguments, null, getExtendedExecutor());
            try {
                getAdvice().enterMethod(context);
                arguments = context.getArguments();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, ENTER_METHOD_ERROR, e);
            }
            return context;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exitMethod(@Advice.Enter ExecuteContext context,
                                      @Advice.This(optional = true) Object target,
                                      @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                                      @Advice.Thrown(readOnly = false) Throwable throwable) {
            context.setTarget(target);
            context.setResult(result);
            context.setThrowable(throwable);

            try {
                getAdvice().exitMethod(context);
                result = context.getResult();
                throwable = context.getThrowable();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, EXIT_METHOD_ERROR, e);
            }
        }
    }

    public static class FullTemplate {
        private static io.manbang.gravity.plugin.Advice advice;
        private static ExtendedExecutor extendedExecutor;

        private FullTemplate() {
            throw new UnsupportedOperationException();
        }

        public static io.manbang.gravity.plugin.Advice getAdvice() {
            return advice;
        }

        public static void setAdvice(io.manbang.gravity.plugin.Advice advice) {
            FullTemplate.advice = advice;
        }

        public static ExtendedExecutor getExtendedExecutor() {
            return extendedExecutor;
        }

        public static void setExtendedExecutor(ExtendedExecutor extendedExecutor) {
            FullTemplate.extendedExecutor = extendedExecutor;
        }

        @Advice.OnMethodEnter
        public static ExecuteContext enterMethod(@Advice.Origin Class<?> targetClass,
                                                 @Advice.This(optional = true) Object target,
                                                 @Advice.Origin Method method,
                                                 @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecuteContext context = getContext(targetClass, target, method, arguments, null, getExtendedExecutor());
            try {
                getAdvice().enterMethod(context);
                arguments = context.getArguments();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, ENTER_METHOD_ERROR, e);
            }
            return context;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exitMethod(@Advice.Enter ExecuteContext context,
                                      @Advice.This(optional = true) Object target,
                                      @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                                      @Advice.Thrown(readOnly = false) Throwable throwable) {
            context.setTarget(target);
            context.setResult(result);
            context.setThrowable(throwable);

            try {
                getAdvice().exitMethod(context);
                result = context.getResult();
                throwable = context.getThrowable();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, EXIT_METHOD_ERROR, e);
            }
        }
    }
}

