package io.manbang.gravity.plugin;

import io.manbang.gravity.bytebuddy.asm.Advice.AllArguments;
import io.manbang.gravity.bytebuddy.asm.Advice.OnMethodEnter;
import io.manbang.gravity.bytebuddy.asm.Advice.OnMethodExit;
import io.manbang.gravity.bytebuddy.asm.Advice.OnNonDefaultValue;
import io.manbang.gravity.bytebuddy.asm.Advice.Origin;
import io.manbang.gravity.bytebuddy.asm.Advice.Return;
import io.manbang.gravity.bytebuddy.asm.Advice.This;
import io.manbang.gravity.bytebuddy.asm.Advice.Thrown;
import io.manbang.gravity.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.manbang.gravity.plugin.Constants.ADVICE_LOGGER_NAME;
import static io.manbang.gravity.plugin.Constants.ENTER_METHOD_ERROR;
import static io.manbang.gravity.plugin.Constants.EXIT_METHOD_ERROR;

/**
 * @author duoliang.zhang
 * @since 2021/8/26 14:12
 */
public class SkipAdviceTemplate {
    public static final String WITH_METHOD_TEMPLATE = "io.manbang.gravity.plugin.SkipAdviceTemplate$WithMethodTemplate";
    public static final String WITHOUT_METHOD_TEMPLATE = "io.manbang.gravity.plugin.SkipAdviceTemplate$WithoutMethodTemplate";
    private static final ThreadLocal<Stack<ExecuteContext>> EXECUTE_CONTEXT_HOLDER = ThreadLocal.withInitial(Stack::new);
    public static final Logger LOGGER = Logger.getLogger(ADVICE_LOGGER_NAME);

    private SkipAdviceTemplate() {
        throw new UnsupportedOperationException();
    }

    public static void pushContext(ExecuteContext context) {
        EXECUTE_CONTEXT_HOLDER.get().push(context);
    }

    public static ExecuteContext popContext() {
        return EXECUTE_CONTEXT_HOLDER.get().pop();
    }

    public static void removeContext() {
        EXECUTE_CONTEXT_HOLDER.remove();
    }

    public static ExecuteContext getContext(Class<?> targetClass, Object target, Method method, Object[] arguments, ExtendedExecutor extendedExecutor) {
        return ExecuteContext.builder()
                .targetClass(targetClass)
                .target(target)
                .method(method)
                .arguments(arguments)
                .extraExecutor(extendedExecutor)
                .build();
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
            SkipAdviceTemplate.WithMethodTemplate.advice = advice;
        }

        public static ExtendedExecutor getExtendedExecutor() {
            return extendedExecutor;
        }

        public static void setExtendedExecutor(ExtendedExecutor extendedExecutor) {
            WithMethodTemplate.extendedExecutor = extendedExecutor;
        }

        @OnMethodEnter(skipOn = OnNonDefaultValue.class)
        public static boolean enterMethod(@Origin Class<?> targetClass,
                                          @This(optional = true) Object target,
                                          @Origin Method method,
                                          @AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecuteContext context = getContext(targetClass, target, method, arguments, getExtendedExecutor());
            pushContext(context);
            try {
                getAdvice().enterMethod(context);
                arguments = context.getArguments();
                return context.isSkipped();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, ENTER_METHOD_ERROR, e);
                return true;
            }
        }

        @OnMethodExit(onThrowable = Throwable.class)
        public static void exitMethod(@This(optional = true) Object target,
                                      @Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                                      @Thrown(readOnly = false) Throwable throwable) {
            ExecuteContext context = popContext();
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

    public static class WithoutMethodTemplate {
        private static io.manbang.gravity.plugin.Advice advice;
        private static ExtendedExecutor extendedExecutor;

        private WithoutMethodTemplate() {
            throw new UnsupportedOperationException();
        }

        public static io.manbang.gravity.plugin.Advice getAdvice() {
            return advice;
        }

        public static void setAdvice(io.manbang.gravity.plugin.Advice advice) {
            WithoutMethodTemplate.advice = advice;
        }

        public static ExtendedExecutor getExtendedExecutor() {
            return extendedExecutor;
        }

        public static void setExtendedExecutor(ExtendedExecutor extendedExecutor) {
            WithoutMethodTemplate.extendedExecutor = extendedExecutor;
        }

        @OnMethodEnter(skipOn = OnNonDefaultValue.class)
        public static boolean enterMethod(@Origin Class<?> targetClass,
                                          @This(optional = true) Object target,
                                          @AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecuteContext context = getContext(targetClass, target, null, arguments, getExtendedExecutor());
            pushContext(context);
            try {
                getAdvice().enterMethod(context);
                arguments = context.getArguments();
                return context.isSkipped();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, ENTER_METHOD_ERROR, e);
                return true;
            }
        }

        @OnMethodExit(onThrowable = Throwable.class)
        public static void exitMethod(@This(optional = true) Object target,
                                      @Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                                      @Thrown(readOnly = false) Throwable throwable) {
            ExecuteContext context = popContext();
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
