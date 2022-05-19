package io.manbang.gravity.plugin.monitor;

import io.manbang.gravity.plugin.Advice;
import io.manbang.gravity.plugin.ExecuteContext;

import java.lang.reflect.Method;

/**
 * @author weilong.hu
 * @since 2022/05/19 11:05
 */
public class AopAdvice implements Advice {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(AopAdvice.class.getName());

    @Override
    public void enterMethod(ExecuteContext context) {
        final Method method = context.getMethod();
        final Object[] argument = context.getArguments();
        final StringBuilder builder = new StringBuilder();
        builder.append("method enter:").append(method.getName());
        for (int i = 0; i < argument.length; i++) {
            builder.append(" arg  number:").append(i).append(" arg :").append(argument[i]);
        }
        log.info(builder.toString());
    }

    @Override
    public void exitMethod(ExecuteContext context) {
        final Method method = context.getMethod();
        final Object result = context.getResult();
        final StringBuilder builder = new StringBuilder();
        builder.append("method exit:").append(method.getName());
        builder.append("result:").append(result);
        log.info(builder.toString());
    }
}
