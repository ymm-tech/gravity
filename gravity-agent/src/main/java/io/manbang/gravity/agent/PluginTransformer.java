package io.manbang.gravity.agent;

import io.manbang.gravity.bytebuddy.ByteBuddy;
import io.manbang.gravity.bytebuddy.agent.builder.AgentBuilder;
import io.manbang.gravity.bytebuddy.asm.Advice;
import io.manbang.gravity.bytebuddy.description.field.FieldDescription;
import io.manbang.gravity.bytebuddy.description.method.MethodDescription;
import io.manbang.gravity.bytebuddy.description.type.TypeDescription;
import io.manbang.gravity.bytebuddy.dynamic.ClassFileLocator;
import io.manbang.gravity.bytebuddy.dynamic.DynamicType;
import io.manbang.gravity.bytebuddy.dynamic.loading.ClassInjector;
import io.manbang.gravity.bytebuddy.implementation.MethodDelegation;
import io.manbang.gravity.bytebuddy.implementation.SuperMethodCall;
import io.manbang.gravity.bytebuddy.implementation.bind.annotation.Morph;
import io.manbang.gravity.bytebuddy.matcher.ElementMatcher;
import io.manbang.gravity.bytebuddy.pool.TypePool;
import io.manbang.gravity.bytebuddy.utility.JavaModule;
import io.manbang.gravity.plugin.AdviceTemplate;
import io.manbang.gravity.plugin.AgentPluginClassLoader;
import io.manbang.gravity.plugin.EmptyExtendedExecutor;
import io.manbang.gravity.plugin.ExtendedExecutor;
import io.manbang.gravity.plugin.GravityUtils;
import io.manbang.gravity.plugin.Interceptor;
import io.manbang.gravity.plugin.InterceptorFactory;
import io.manbang.gravity.plugin.MorphingCallable;
import io.manbang.gravity.plugin.Plugin;
import io.manbang.gravity.plugin.PluginDefine;
import io.manbang.gravity.plugin.SkipAdviceTemplate;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.manbang.gravity.bytebuddy.jar.asm.Opcodes.ACC_PRIVATE;
import static io.manbang.gravity.bytebuddy.jar.asm.Opcodes.ACC_VOLATILE;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.isConstructor;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.not;

@Log
class PluginTransformer implements AgentBuilder.Transformer {
    private static final String SET_ADVICE_METHOD_NAME = "setAdvice";
    private static final String SET_EXTENDED_EXECUTOR_METHOD_NAME = "setExtendedExecutor";
    private static final Map<ClassLoader, AgentPluginClassLoader> CLASS_DELEGATION_LOADER_MAP = new ConcurrentHashMap<>();
    private static final Map<ClassLoader, AgentPluginClassLoader> CLASS_LOADER_MAP = new ConcurrentHashMap<>();
    private static final Map<ClassLoader, Map<String, AtomicBoolean>> EXTEND_CLASS_LOADER_CLASS_FIELD_MAP = new ConcurrentHashMap<>();
    private final PluginDefine pluginDefine;

    PluginTransformer(PluginDefine pluginDefine) {
        this.pluginDefine = pluginDefine;
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
        final String targetClass = typeDescription.getName();
        log.info(targetClass);

        // 一个插件定义，只能支持一类目标（比如MySQL、Redis）的植入
        AgentPluginClassLoader agentClassLoader = getClassLoader(classLoader, targetClass);
        if (agentClassLoader == null) {
            return builder;
        }
        if (pluginDefine.getWitness().saw(agentClassLoader)) {
            // 但每个插件可以有多个不同的植入点
            for (Plugin plugin : pluginDefine.getPlugins()) {
                switch (plugin.getType()) {
                    case ADVICE:
                        builder = instrumentAdvice(builder, agentClassLoader, plugin, classLoader);
                        break;
                    case INTERCEPTOR:
                        builder = instrumentInterceptor(builder, agentClassLoader, plugin);
                        break;
                    case INTERCEPTOR_INSTANCE:
                        builder = instrumentInterceptorInstance(builder, agentClassLoader, plugin);
                        break;
                    default:
                        // do nothing
                        break;
                }
                builder = extend(builder, typeDescription, classLoader, targetClass, plugin);
            }
        } else {
            log.warning(() -> "witness: " + pluginDefine.getName());
        }

        // 在这个地方启动，为的是得到应用的 ClassLoader，只会执行一次
        GravityServiceBoot.INSTANCE.startServices(agentClassLoader);

        return builder;
    }

    private AgentPluginClassLoader getClassLoader(ClassLoader classLoader, String targetClass) {
        AgentPluginClassLoader agentClassLoader;
        if (classLoader instanceof AgentPluginClassLoader) {
            if (pluginDefine.ignoreEnhanceAgentClass()) {
                log.info(String.format("The current classLoader is instanceof AgentPluginClassLoader , pluginDefine: %s is user space, ignore transform: %s", pluginDefine.getName(), targetClass));
                agentClassLoader = null;
            } else {
                log.info(String.format("The current classLoader is instanceof AgentPluginClassLoader , pluginDefine: %s , transform: %s", pluginDefine.getName(), targetClass));
                agentClassLoader = (AgentPluginClassLoader) classLoader;
            }
        } else {
            if (pluginDefine.isDelegated()) {
                log.info(String.format("The current classLoader is %s , pluginDefine: %s is delegated , transform: %s", classLoader, pluginDefine.getName(), targetClass));
                agentClassLoader = CLASS_DELEGATION_LOADER_MAP.computeIfAbsent(classLoader, c -> new AgentPluginClassLoader(c, true));
            } else {
                log.info(String.format("The current classLoader is %s , pluginDefine: %s , transform: %s", classLoader, pluginDefine.getName(), targetClass));
                agentClassLoader = CLASS_LOADER_MAP.computeIfAbsent(classLoader, c -> new AgentPluginClassLoader(c, false));
            }
        }
        return agentClassLoader;
    }

    /**
     * 额外的扩展原有class
     */
    private DynamicType.Builder<?> extend(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, String targetClass, Plugin plugin) {
        if (plugin.getExtendFields() != null) {
            final Map<String, AtomicBoolean> extendedClassFieldMap = EXTEND_CLASS_LOADER_CLASS_FIELD_MAP.computeIfAbsent(classLoader, c -> new ConcurrentHashMap<>());
            final Set<String> declaredFieldNames = getDeclaredFieldNames(typeDescription);
            for (String extendField : plugin.getExtendFields()) {
                final String classField = String.format("%s.%s", targetClass, extendField);
                final AtomicBoolean extended = extendedClassFieldMap.computeIfAbsent(classField, t -> new AtomicBoolean(false));
                //防止重复织入field
                if (declaredFieldNames.contains(extendField)) {
                    log.warning(String.format("The current class already has field,ignore this weaving field : %s", classField));
                } else if (extended.compareAndSet(false, true)) {
                    builder = builder.defineField(extendField, Object.class, ACC_PRIVATE | ACC_VOLATILE);
                    log.info(String.format("The current class weaves into the new field successfully,field : %s", classField));
                } else {
                    log.warning(String.format("The current class has been woven into the extend field : %s", classField));
                }
            }
        }
        return builder;
    }

    private Set<String> getDeclaredFieldNames(TypeDescription typeDescription) {
        final Set<String> declaredFieldNames = new HashSet<>();
        //不使用stream 防止提前触发一些类的加载
        for (FieldDescription.InDefinedShape declaredField : typeDescription.getDeclaredFields()) {
            declaredFieldNames.add(declaredField.getName());
        }
        return declaredFieldNames;
    }

    private DynamicType.Builder<?> instrumentInterceptorInstance(DynamicType.Builder<?> builder, AgentPluginClassLoader acl, Plugin plugin) {
        ElementMatcher<MethodDescription> methodMatcher = excludeObjectMethod(plugin);
        Object interceptor = plugin.getInterceptor();
        final ExtendedExecutor extendedExecutor = extendedExecutor(plugin, acl, acl.getParent());
        if (interceptor instanceof Interceptor) {
            if (plugin.isDefineMethod()) {
                builder = builder.defineMethod(plugin.methodName(), Object.class, plugin.modifiers())
                        .withParameters(Object[].class)
                        .intercept(MethodDelegation.withDefaultConfiguration().to(InterceptorFactory.defineMethod((Interceptor) interceptor, extendedExecutor)));
            } else if (plugin.isConstructorAdvised()) {
                builder = builder.constructor(methodMatcher).intercept(SuperMethodCall.INSTANCE.andThen(
                        MethodDelegation.withDefaultConfiguration().to(InterceptorFactory.constructor((Interceptor) interceptor, extendedExecutor))));
            } else {
                builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(MorphingCallable.class))
                        .to(InterceptorFactory.method((Interceptor) interceptor, extendedExecutor)));
            }
        } else {
            builder = builder.method(methodMatcher).intercept(MethodDelegation.to(interceptor));
        }
        return builder;
    }

    @SneakyThrows
    private DynamicType.Builder<?> instrumentInterceptor(DynamicType.Builder<?> builder, ClassLoader acl, Plugin plugin) {
        ElementMatcher<MethodDescription> methodMatcher = excludeObjectMethod(plugin);
        Class<?> interceptorClass = acl.loadClass(plugin.getInterceptorClassName());
        final ExtendedExecutor extendedExecutor = extendedExecutor(plugin, acl, acl.getParent());
        if (Interceptor.class.isAssignableFrom(interceptorClass)) {
            if (plugin.isDefineMethod()) {
                builder = builder.defineMethod(plugin.methodName(), Object.class, plugin.modifiers())
                        .withParameters(Object[].class)
                        .intercept(MethodDelegation.withDefaultConfiguration().to(InterceptorFactory.defineMethod(interceptorClass, extendedExecutor)));
            }
            if (plugin.isConstructorAdvised()) {
                builder = builder.constructor(methodMatcher).intercept(SuperMethodCall.INSTANCE.andThen(
                        MethodDelegation.withDefaultConfiguration().to(InterceptorFactory.constructor(interceptorClass, extendedExecutor))));
            } else {
                builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(MorphingCallable.class))
                        .to(InterceptorFactory.method(interceptorClass, extendedExecutor)));
            }
        } else {
            builder = builder.method(methodMatcher).intercept(MethodDelegation.to(interceptorClass));
        }
        return builder;
    }

    @SneakyThrows
    private DynamicType.Builder<?> instrumentAdvice(DynamicType.Builder<?> builder, ClassLoader agentClassLoader, Plugin plugin, ClassLoader classLoader) {
        ElementMatcher<MethodDescription> methodMatcher = excludeObjectMethod(plugin);
        String adviceClassName = plugin.getAdviceClassName();
        Class<?> adviceClass = agentClassLoader.loadClass(adviceClassName);

        if (io.manbang.gravity.plugin.Advice.class.isAssignableFrom(adviceClass)) {
            // 动态创建的 AdviceTemplate
            Class<?> adviceTemplateClass = prepareAdviceTemplateClass(classLoader, plugin, adviceClass);

            // 动态生成的类，通过 ClassLoader#getResource的方式，是找不到，因此要指定 ClassFileLocator
            String adviceTemplateClassName = getAdviceTemplateClassName(adviceClassName, classLoader);
            ClassFileLocator classFileLocator = ClassFileLocator.Simple.of(adviceTemplateClassName, GravityUtils.getAdviceTemplateByteCodes(adviceTemplateClassName));

            builder = builder.visit(Advice.to(adviceTemplateClass, classFileLocator).on(methodMatcher));
        } else {
            builder = builder.visit(Advice.to(adviceClass).on(methodMatcher));
        }
        return builder;
    }

    @SneakyThrows
    private Class<?> prepareAdviceTemplateClass(ClassLoader classLoader, Plugin plugin, Class<?> adviceClass) {
        String adviceTemplateClassName = getAdviceTemplateClassName(plugin.getAdviceClassName(), classLoader);
        try {
            return classLoader.loadClass(adviceTemplateClassName);
        } catch (ClassNotFoundException ignore) {
            // 还没有加载过
        }

        TypePool typePool = TypePool.Default.of(classLoader);
        String baseTemplateClassName = getBaseTemplateClassName(plugin);

        TypeDescription templateTypeDescription = typePool.describe(baseTemplateClassName).resolve();

        byte[] templateClassBytes = new ByteBuddy().redefine(templateTypeDescription, ClassFileLocator.ForClassLoader
                .of(classLoader))
                .name(adviceTemplateClassName)
                .make().getBytes();

        ClassInjector.UsingUnsafe.Factory.resolve(GravityUtils.getInstrumentation())
                .make(classLoader, null)
                .injectRaw(Collections.singletonMap(adviceTemplateClassName, templateClassBytes));

        // byte buddy 的 ClassFileLocator 要用到，它需要指定类的字节码信息，用来解析
        GravityUtils.putByteCodes(adviceTemplateClassName, templateClassBytes);
        final Class<?> adviceTemplateClass = classLoader.loadClass(adviceTemplateClassName);
        adviceTemplateClass.getDeclaredMethod(SET_ADVICE_METHOD_NAME, io.manbang.gravity.plugin.Advice.class).invoke(null, io.manbang.gravity.plugin.Advice.of(adviceClass));

        final ExtendedExecutor extendedExecutor = extendedExecutor(plugin, adviceClass.getClassLoader(), classLoader);
        adviceTemplateClass.getDeclaredMethod(SET_EXTENDED_EXECUTOR_METHOD_NAME, ExtendedExecutor.class).invoke(null, extendedExecutor);
        return adviceTemplateClass;
    }

    private String getAdviceTemplateClassName(String adviceClass, ClassLoader classLoader) {
        final String simpleName = classLoader.getClass().getSimpleName();
        final String hashCode = Integer.toHexString(System.identityHashCode(classLoader));
        return String.format("%s_%s_%s$Template", adviceClass, simpleName, hashCode);
    }

    private String getBaseTemplateClassName(Plugin plugin) {
        if (plugin.isConstructorAdvised()) {
            if (plugin.isWithConstructor()) {
                return AdviceTemplate.WITH_CONSTRUCTOR_TEMPLATE;
            } else {
                return AdviceTemplate.MINI_TEMPLATE;
            }
        } else if (plugin.isSkipEnabled()) {
            // 可以终止业务逻辑执行的Advice，必须捕捉异常，否则有可能导致栈溢出
            if (plugin.isWithMethod()) {
                return SkipAdviceTemplate.WITH_METHOD_TEMPLATE;
            } else {
                return SkipAdviceTemplate.WITHOUT_METHOD_TEMPLATE;
            }
        } else {
            if (plugin.isWithThrowable() && plugin.isWithMethod()) {
                return AdviceTemplate.FULL_TEMPLATE;
            } else if (plugin.isWithThrowable()) {
                return AdviceTemplate.WITH_THROWABLE_TEMPLATE;
            } else if (plugin.isWithMethod()) {
                return AdviceTemplate.WITH_METHOD_TEMPLATE;
            } else {
                return AdviceTemplate.MINI_TEMPLATE;
            }
        }
    }

    private ElementMatcher<MethodDescription> excludeObjectMethod(Plugin plugin) {
        ElementMatcher<MethodDescription> methodMatcher = plugin.getMethodMatcher();
        if (pluginDefine.ignoreObjectMethod()) {
            methodMatcher = not(isDeclaredBy(Object.class)).and(methodMatcher);
        }
        if (plugin.isConstructorAdvised()) {
            methodMatcher = isConstructor().and(methodMatcher);
        }
        return methodMatcher;
    }

    @SneakyThrows
    private ExtendedExecutor extendedExecutor(Plugin plugin, ClassLoader source, ClassLoader target) {
        final String extendedExecutor = plugin.getExtendedExecutor();
        if (extendedExecutor == null || extendedExecutor.isEmpty()) {
            return EmptyExtendedExecutor.INSTANCE;
        } else {
            //从source提取extendedExecutor字节码，织入到target里面
            ClassInjector.UsingUnsafe.Factory.resolve(GravityUtils.getInstrumentation())
                    .make(target, null)
                    .injectRaw(Collections.singletonMap(extendedExecutor, ClassFileLocator.ForClassLoader
                            .of(source).locate(extendedExecutor).resolve()));
            final Class<?> extendedExecutorClazz = target.loadClass(extendedExecutor);
            if (ExtendedExecutor.class.isAssignableFrom(extendedExecutorClazz)) {
                return (ExtendedExecutor) extendedExecutorClazz.newInstance();
            } else {
                log.warning(String.format("%s is not a subtype of ExtendedExecutor", extendedExecutor));
                return EmptyExtendedExecutor.INSTANCE;
            }
        }
    }
}
