package io.manbang.gravity.plugin;

import lombok.Builder;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


/**
 * 方法执行上下文，用于在 {@link Advice} 和 {@link Interceptor}的回调方法间传递上下文信息
 *
 * @author duoliang.zhang
 * @since 2020/8/25 16:46
 */
@Builder
@SuppressWarnings("unchecked")
@ToString(of = {"targetClass", "method"})
public class ExecuteContext {
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    /**
     * 附件信息
     */
    private final Map<String, Object> attachments = new HashMap<>();
    /**
     * 拦截对象的类型
     */
    private final Class<?> targetClass;
    /**
     * 被拦截的方法
     */
    private final Method method;
    /**
     * 被拦截的构造器
     */
    private final Constructor<?> constructor;
    /**
     * 实参列表
     */
    private Object[] arguments;
    /**
     * 拦截的对象实例
     */
    private Object target;
    /**
     * 返回值结果，可以用来存储原方法返回的结果，或者用户重定向的覆盖结果
     */
    private Object result;
    /**
     * 异常，可以用来存储原方法抛出的异常，或者用户自定义重新抛出去的异常
     */
    private Throwable throwable;
    /**
     * 跳过业务逻辑执行
     */
    private boolean skipped;

    /**
     * 扩展执行器
     */
    private ExtendedExecutor extraExecutor;

    /**
     * 业务跳过执行的话，回调处理，{@link Advice} 插件有效，别用在 {@link Interceptor}
     *
     * @param consumer 回调函数
     * @return 上下文
     */
    public ExecuteContext onSkip(Consumer<ExecuteContext> consumer) {
        if (skipped) {
            consumer.accept(this);
        }
        return this;
    }

    /**
     * 业务继续执行的话，回调处理，{@link Advice} 插件有效，别用在 {@link Interceptor}
     *
     * @param consumer 回调函数
     * @return 上下文
     */
    public ExecuteContext onContinue(Consumer<ExecuteContext> consumer) {
        if (!skipped) {
            consumer.accept(this);
        }
        return this;
    }

    public void extraExecute() {
        extraExecutor.execute(this);
    }

    public ExtendedExecutor getExtraExecutor() {
        return extraExecutor;
    }

    /**
     * 获取被植入的方法
     *
     * @return 被植入方法
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 获取被植入的构造器
     *
     * @return 被植入的构造器
     */
    public <T> Constructor<T> getConstructor() {
        return (Constructor<T>) constructor;
    }

    /**
     * 获取被植入代码类
     *
     * @param <T> 实际类类型
     * @return 被植入方法所在的类
     */
    public <T> Class<T> getTargetClass() {
        return (Class<T>) targetClass;
    }

    /**
     * 判断是否需要跳过业务逻辑执行
     *
     * @return 如果跳过业务逻辑执行，返回<code>true</code>，否则，返回<code>false</code>
     */
    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    /**
     * 跳过业务逻辑执行
     */
    public void skip() {
        this.skipped = true;
    }

    /**
     * 获取被拦截对象的全限定名
     *
     * @return 被拦截类的全限定名
     */
    public String getTargetClassName() {
        return targetClass.getName();
    }

    public ClassLoader getClassLoader() {
        return targetClass.getClassLoader();
    }

    /**
     * 设置新的异常实例
     *
     * @param throwable 新异常
     */
    public void newThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * 这是新的返回值，改变原方法的返回值
     *
     * @param result 返回值
     */
    public void newResult(Object result) {
        this.result = result;
        this.throwable = null;
    }

    /**
     * 获取附件列表，不能修改
     *
     * @return 附件列表
     */
    public Map<String, Object> getAttachments() {
        return Collections.unmodifiableMap(attachments);
    }

    /**
     * 像上下文中添加附件信息，可用来在拦截器重传递信息
     *
     * @param name  附件名称
     * @param value 附件
     * @return 执行上下文，方便连续添加附件
     */
    public ExecuteContext addAttachment(String name, Object value) {
        attachments.put(name, value);
        return this;
    }

    /**
     * 清空附件信息
     */
    public void clearAttachments() {
        attachments.clear();
    }

    /**
     * 获取指定名称的附件，如果不存在，是可能为空值的
     *
     * @param name 附件名
     * @param <T>  附件类型
     * @return 附件
     */
    public <T> T getAttachment(String name) {
        return (T) attachments.get(name);
    }

    /**
     * 获取返回值，可能是修改之后的
     *
     * @param <T> 返回值类型
     * @return 返回值
     */
    public <T> T getResult() {
        return (T) result;
    }

    /**
     * 设置方法返回结果，内部用，外不用请用 {@link ExecuteContext#newResult(Object)}
     *
     * @param result 返回结果
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 获取指定位置实参
     *
     * @param index 实参索引
     * @param <T>   实参类型
     * @return 实参
     */
    public <T> T getArgument(int index) {
        return (T) arguments[index];
    }

    /**
     * 获取第一个实参
     *
     * @param <T> 参数类型
     * @return 实参
     */
    public <T> T getArgument() {
        return getArgument(0);
    }

    /**
     * 修改指定索引位置的实参
     *
     * @param index 实参索引
     * @param arg   新参数
     */
    public void newArgument(int index, Object arg) {
        arguments[index] = arg;
    }

    /**
     * 修改实参列表
     *
     * @param arguments 新的实参列表
     */
    public void newArguments(Object... arguments) {
        this.arguments = arguments;
    }

    /**
     * 获取当前类指定名称字段值，字段反射会被缓存
     *
     * @param name 字段名
     * @param <T>  字段类型
     * @return 字段值
     */
    public <T> T getFieldValue(String name) {
        Field field = getField(name);
        try {
            return (T) field.get(target);
        } catch (IllegalAccessException e) {
            throw new GravityException(e);
        }
    }

    /**
     * 强行设置字段值
     *
     * @param name  字段名称
     * @param value 要设置的字段值
     */
    public void setFieldValue(String name, Object value) {
        Field field = getField(name);
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true); // NOSONAR
        }

        try {
            field.set(getTarget(), value); // NOSONAR
        } catch (IllegalAccessException e) {
            throw new GravityException(e);
        }
    }

    private Field getField(String name) {
        Class<?> clazz;
        //静态方法无target
        if (Objects.nonNull(target)) {
            //优先从当前target检索field 避免field重名情况
            clazz = target.getClass();
        } else {
            clazz = this.targetClass;
        }
        final Map<String, Field> fieldMap = FIELD_CACHE.computeIfAbsent(clazz, t -> new ConcurrentHashMap<>());
        return fieldMap.computeIfAbsent(name, n -> {
            Class<?> c = clazz;
            do {
                try {
                    Field field = c.getDeclaredField(n);
                    field.setAccessible(true);// NOSONAR
                    return field;
                } catch (NoSuchFieldException e) {
                    c = c.getSuperclass();
                }
            }
            while (c != null && c != Object.class);

            throw new GravityException(new NoSuchFieldException(n));
        });
    }

    /**
     * 调用指定名称方法
     *
     * @param name           方法名
     * @param parameterTypes 形参类型列表
     * @param args           实参列表
     * @param <T>            返回值类型
     * @return 返回值
     */
    public <T> T invokeMethod(String name, Class<?>[] parameterTypes, Object[] args) {
        Method m = getMethod(name, parameterTypes);

        try {
            return (T) m.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new GravityException(e);
        } catch (InvocationTargetException e) {
            throw new GravityException(e.getCause());
        }
    }

    private Method getMethod(String name, Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder();
        for (Class<?> parameterType : parameterTypes) {
            sb.append(parameterType.getName()).append("|");
        }
        return METHOD_CACHE.computeIfAbsent(String.format("%s#%s(%s)", targetClass.getName(), name, sb), n -> {
            Class<?> clazz = targetClass;
            do {
                try {
                    Method m = clazz.getDeclaredMethod(name, parameterTypes);
                    m.setAccessible(true); // NOSONAR
                    return m;
                } catch (NoSuchMethodException e) {
                    clazz = clazz.getSuperclass();
                }
            } while (clazz != null && clazz != Object.class);
            throw new GravityException(new NoSuchMethodException(name));
        });
    }

    public <T> T getTarget() {
        return (T) target;
    }

    /**
     * 设置被植入对象，如果被植入的方式是静态方法，是<code>null</code>
     *
     * @param target 被植入对象
     */
    public void setTarget(Object target) {
        this.target = target;
    }

    public <T extends Throwable> T getThrowable() {
        return (T) throwable;
    }

    /**
     * 设置方法抛出的异常，如果要修改下异常的，请使用：{@link ExecuteContext#newThrowable(Throwable)}
     *
     * @param throwable 异常
     * @see ExecuteContext#newThrowable(Throwable)
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Object[] getArguments() {
        return arguments;
    }
}

