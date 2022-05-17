package io.manbang.gravity.plugin;


import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.LinkedList;
import java.util.List;

/**
 * 插件，用于植入到应用的代码当中
 *
 * @author 章多亮
 * @since 2020/8/11 9:31
 */
public class Plugin {
    /**
     * 插件类型
     */
    private final PluginType type;
    /**
     * 拦截器全限定名
     */
    private final String adviceClassName;
    /**
     * 拦截器类全限定名
     */
    private final String interceptorClassName;
    /**
     * 待植入的拦截器实例，默认空值
     */
    private final Object interceptor;
    /**
     * 获取插件的方法匹配器，用于Agent定位要植入代码的方法
     */
    private final ElementMatcher<MethodDescription> methodMatcher;
    /**
     * 是否捕捉异常
     */
    private boolean withThrowable;
    /**
     * 是否需要被拦截的方法
     */
    private boolean withMethod;
    /**
     * 是否需要被拦截的构造器
     */
    private boolean withConstructor;
    /**
     * {@link Advice} 控制Advice是否可以提前终止业务逻辑执行
     */
    private boolean skipEnabled;

    /**
     * 是否是增强构造函数
     */
    private boolean constructorAdvised;

    /**
     * 是否是定义方法
     */
    private boolean defineMethod;
    /**
     * 定义方法修饰符
     */
    private int modifiers;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 拓展执行器
     */
    private String extendedExecutor;

    /**
     * 是否需要植入扩展field
     */
    private String[] extendFields;

    private Plugin(PluginType type, ElementMatcher<MethodDescription> methodMatcher, String adviceClassName, String interceptorClassName, Object interceptor) {
        this.type = type;
        this.methodMatcher = methodMatcher;
        this.interceptor = interceptor;
        this.adviceClassName = adviceClassName;
        this.interceptorClassName = interceptorClassName;
    }

    public static <T> Plugin advice(ElementMatcher<MethodDescription> methodMatcher, Class<T> adviceClass) {
        return new Plugin(PluginType.ADVICE, methodMatcher, adviceClass.getName(), null, null);
    }

    public static <T> Plugin interceptor(ElementMatcher<MethodDescription> methodMatcher, Class<T> interceptorClass) {
        return new Plugin(PluginType.INTERCEPTOR, methodMatcher, null, interceptorClass.getName(), null);
    }

    public static <T> Plugin interceptor(Class<T> interceptorClass) {
        return new Plugin(PluginType.INTERCEPTOR, ElementMatchers.none(), null, interceptorClass.getName(), null);
    }

    public static Plugin advice(ElementMatcher<MethodDescription> methodMatcher, String adviceClassName) {
        return new Plugin(PluginType.ADVICE, methodMatcher, adviceClassName, null, null);
    }

    public static Plugin interceptor(ElementMatcher<MethodDescription> methodMatcher, String interceptorClassName) {
        return new Plugin(PluginType.INTERCEPTOR, methodMatcher, null, interceptorClassName, null);
    }

    public static Plugin interceptor(String interceptorClassName) {
        return new Plugin(PluginType.INTERCEPTOR, ElementMatchers.none(), null, interceptorClassName, null);
    }

    public static <T> Plugin interceptorInstance(ElementMatcher<MethodDescription> methodMatcher, T interceptor) {
        return new Plugin(PluginType.INTERCEPTOR_INSTANCE, methodMatcher, null, null, interceptor);
    }


    public static <T> Plugin interceptorInstance(T interceptor) {
        return new Plugin(PluginType.INTERCEPTOR_INSTANCE, ElementMatchers.none(), null, null, interceptor);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String[] getExtendFields() {
        return extendFields;
    }

    /**
     * 捕捉异常
     */
    public Plugin withThrowable() {
        this.withThrowable = true;
        return this;
    }

    /**
     * 获取被拦截的方法
     */
    public Plugin withMethod() {
        this.withMethod = true;
        return this;
    }

    /**
     * 获取被拦截的构造器
     */
    public Plugin withConstructor() {
        this.withConstructor = true;
        this.constructorAdvised = true;
        return this;
    }

    /**
     * 增强构造函数
     */
    public Plugin constructorAdvised() {
        this.constructorAdvised = true;
        return this;
    }

    public Plugin defineMethod(String methodName, int modifiers) {
        if (this.type != PluginType.INTERCEPTOR && this.type != PluginType.INTERCEPTOR_INSTANCE) {
            throw new UnsupportedOperationException("only interceptors support defining methods!");
        }
        this.modifiers = modifiers;
        this.methodName = methodName;
        this.defineMethod = true;
        return this;
    }

    /**
     * {@link Advice} 用来控制是否执行业务逻辑，如果启用， {@link Advice#enterMethod(ExecuteContext)} 的返回值有效
     *
     * @return 插件本身
     */
    public Plugin enableSkip() {
        this.skipEnabled = true;
        return this;
    }

    /**
     * 增加拓展field
     */
    public Plugin withExtendFields(String... fields) {
        this.extendFields = fields;
        return this;
    }

    public Plugin withExtendedExecutor(String extendedExecutor) {
        this.extendedExecutor = extendedExecutor;
        return this;
    }

    public PluginType getType() {
        return type;
    }

    public String getAdviceClassName() {
        return adviceClassName;
    }

    public String getInterceptorClassName() {
        return interceptorClassName;
    }

    public Object getInterceptor() {
        return interceptor;
    }

    public ElementMatcher<MethodDescription> getMethodMatcher() {
        return methodMatcher;
    }

    public boolean isWithThrowable() {
        return withThrowable;
    }

    public boolean isWithMethod() {
        return withMethod;
    }

    public boolean isWithConstructor() {
        return withConstructor;
    }

    public boolean isSkipEnabled() {
        return skipEnabled;
    }

    public boolean isConstructorAdvised() {
        return constructorAdvised;
    }

    public String getExtendedExecutor() {
        return extendedExecutor;
    }

    public boolean isDefineMethod() {
        return defineMethod;
    }

    public String methodName() {
        return methodName;
    }

    public int modifiers() {
        return modifiers;
    }

    public static class Builder {
        private static final Plugin[] EMPTY = new Plugin[0];
        private final List<Plugin> plugins = new LinkedList<>();

        private Builder() {
        }

        public Builder advice(ElementMatcher<MethodDescription> methodMatcher, String adviceClassName) {
            plugins.add(Plugin.advice(methodMatcher, adviceClassName));
            return this;
        }

        public Builder interceptor(ElementMatcher<MethodDescription> methodMatcher, String interceptorClassName) {
            plugins.add(Plugin.interceptor(methodMatcher, interceptorClassName));
            return this;
        }

        public <T> Builder advice(ElementMatcher<MethodDescription> methodMatcher, Class<T> adviceClass) {
            plugins.add(Plugin.advice(methodMatcher, adviceClass.getName()));
            return this;
        }


        public <T> Builder interceptor(ElementMatcher<MethodDescription> methodMatcher, Class<T> interceptorClass) {
            plugins.add(Plugin.interceptor(methodMatcher, interceptorClass.getName()));
            return this;
        }

        public <T> Builder interceptorInstance(ElementMatcher<MethodDescription> methodMatcher, T interceptor) {
            plugins.add(Plugin.interceptorInstance(methodMatcher, interceptor));
            return this;
        }

        public Plugin[] build() {
            return plugins.toArray(EMPTY);
        }
    }
}
