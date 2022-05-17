package io.manbang.gravity.agent;


import io.manbang.gravity.plugin.AgentBuilderCustomizer;
import io.manbang.gravity.plugin.AgentOptions;
import io.manbang.gravity.plugin.AgentPluginClassLoader;
import io.manbang.gravity.plugin.GravityUtils;
import io.manbang.gravity.plugin.PluginDefine;
import io.manbang.gravity.plugin.Services;
import lombok.extern.java.Log;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.logging.Level;

import static net.bytebuddy.matcher.ElementMatchers.isSynthetic;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;


/**
 * 插件代理，以java agent的形式，运行在应用侧
 *
 * @author duoliang.zhang
 * @since 2020/08/11 10:17:04
 */
@Log
public class GravityAgent {
    private static ResettableClassFileTransformer transformer;

    private GravityAgent() {
        throw new UnsupportedOperationException("插件代理不支持实例化");
    }

    public static void premain(String argument, Instrumentation instrumentation) {
        instrument(argument, instrumentation);
    }

    public static void agentmain(String argument, Instrumentation instrumentation) {
        instrument(argument, instrumentation);
    }

    private static void instrument(String argument, Instrumentation instrumentation) {
        System.setProperty("agent.options", argument);
        GravityUtils.setInstrumentation(instrumentation);

        // 现在全部插件
        PluginDownloader.downloadPlugins(instrumentation, AgentOptions.INSTANCE);

        AgentPluginClassLoader pluginClassLoader = new AgentPluginClassLoader();
        GravityUtils.setAgentPluginClassLoader(pluginClassLoader);

        AgentBuilder builder = getAgentBuilder(pluginClassLoader);

        List<PluginDefine> pluginDefines = getPluginDefines(pluginClassLoader);
        for (PluginDefine plugin : pluginDefines) {
            log.info(() -> "加载插件：" + plugin.getName());
            try {
                builder = plugin.prepare(builder, instrumentation);
            } catch (Exception e) {
                log.log(Level.INFO, e, () -> String.format("加载插件：%s出现异常，不影响后续织入", plugin.getName()));
            }
            builder = builder.type(plugin.getTypeMatcher()).transform(new PluginTransformer(plugin));
        }

        transformer = builder.installOn(instrumentation);
    }

    private static AgentBuilder getAgentBuilder(AgentPluginClassLoader classLoader) {
        ByteBuddy byteBuddy = new ByteBuddy();
        AgentBuilder builder = new AgentBuilder.Default(byteBuddy)
                // .with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED)
                .ignore(nameStartsWith("io.manbang.gravity.bytebuddy.")
                        .or(nameStartsWith("org.slf4j."))
                        .or(nameStartsWith("org.groovy."))
                        .or(nameContains("javassist"))
                        .or(nameContains(".asm."))
                        .or(nameContains(".reflectasm."))
                        .or(nameStartsWith("sun.reflect"))
                        .or(nameStartsWith("com.intellij"))
                        .or(isSynthetic()));

        AgentBuilderCustomizer builderCustomizer = AgentBuilderCustomizer.customizer(classLoader);
        builder = builderCustomizer.customize(AgentOptions.INSTANCE, builder);
        return builder;
    }

    private static List<PluginDefine> getPluginDefines(AgentPluginClassLoader pluginClassLoader) {
        List<PluginDefine> pluginDefines = Services.loadAll(PluginDefine.class, pluginClassLoader);

        if (pluginDefines.isEmpty()) {
            log.warning("没有加载到任何插件定义！！！");
        }

        return pluginDefines;
    }

    public static boolean reset() {
        return transformer.reset(GravityUtils.getInstrumentation(),
                AgentBuilder.RedefinitionStrategy.RETRANSFORMATION,
                AgentBuilder.RedefinitionStrategy.DiscoveryStrategy.SinglePass.INSTANCE,
                AgentBuilder.RedefinitionStrategy.BatchAllocator.ForTotal.INSTANCE,
                AgentBuilder.RedefinitionStrategy.Listener.StreamWriting.toSystemError());
    }

}
