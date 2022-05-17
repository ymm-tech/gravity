package io.manbang.gravity.plugin;



import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.instrument.Instrumentation;

/**
 * 插件定义，描述一个插件的能力，Advice可以作用于哪些类的哪些方法
 *
 * @author 章多亮
 * @since 2020/08/06
 */
public interface PluginDefine extends Ordered, Named {
    /**
     * 很多插件需要提前将一些类或者接口初始化，可以覆盖此方法
     *
     * @param builder         代理构建器
     * @param instrumentation 代码植入器
     */
    default AgentBuilder prepare(AgentBuilder builder, Instrumentation instrumentation) {
        return builder;
    }

    /**
     * 获取插件的类型匹配器，用于Agent定位要植入代码的类型
     *
     * @return 类匹配器
     */
    ElementMatcher<TypeDescription> getTypeMatcher();

    /**
     * 获取插件列表
     *
     * @return 插件列表
     */
    Plugin[] getPlugins();

    /**
     * 判断是否忽略Object对象方法
     *
     * @return 如果不需要增强 Object 方法，返回<code>true</code>
     */
    default boolean ignoreObjectMethod() {
        return true;
    }

    /**
     * 返回当前插件的目击者，用于判断是否支持当前应用的代码增强，默认直接目击
     *
     * @return 目击者
     */
    default Witness getWitness() {
        return Witness.always();
    }

    /**
     * 是否双亲委托
     */
    default boolean isDelegated() {
        return false;
    }

    /**
     * 忽略增强agent classloader下的class
     */
    default boolean ignoreEnhanceAgentClass() {
        return true;
    }
}
