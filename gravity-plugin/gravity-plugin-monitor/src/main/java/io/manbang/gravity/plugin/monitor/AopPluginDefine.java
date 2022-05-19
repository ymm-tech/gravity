package io.manbang.gravity.plugin.monitor;

import io.manbang.gravity.plugin.Plugin;
import io.manbang.gravity.plugin.PluginDefine;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author weilong.hu
 * @since 2022/05/19 10:55
 */
public class AopPluginDefine implements PluginDefine {
    @Override
    public ElementMatcher<TypeDescription> getTypeMatcher() {
        return ElementMatchers.named("io.manbang.gravity.trade.Driver")
                .or(ElementMatchers.named("io.manbang.gravity.trade.Shippers"));
    }

    @Override
    public Plugin[] getPlugins() {
        return new Plugin[]{Plugin.advice(ElementMatchers.isMethod(), "io.manbang.gravity.plugin.monitor.AopAdvice").withMethod()};
    }
}
