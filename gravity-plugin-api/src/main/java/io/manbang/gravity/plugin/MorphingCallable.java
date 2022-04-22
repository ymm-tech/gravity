package io.manbang.gravity.plugin;

/**
 * 用于修改入参的调用器
 *
 * @author duoliang.zhang
 * @since 2020/8/25 16:41
 */
public interface MorphingCallable<T> {
    /**
     * 执行方法调用
     *
     * @param args 实参，传入前可以修改
     * @return 业务方法返回值
     */
    T call(Object... args);
}

