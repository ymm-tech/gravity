package io.manbang.gravity.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author duoliang.zhang
 * @since 2021/8/26 15:02
 */
class AdviceHolder {
    private static final Map<Class<?>, Advice> ADVICES = new ConcurrentHashMap<>();

    private AdviceHolder() {
        throw new UnsupportedOperationException();
    }

    /**
     * 将原始 {@link Advice} 做一次包装，如果Advice支持开关的话，会封装成 {@link SwitcherAdvice}，否则直接返回
     *
     * @param advice 切面插件
     * @return 如果是可以开关控制的 {@link Advice}，返回包装后的 {@link SwitcherAdvice}
     */
    private static Advice switcher(Advice advice) {
        return advice.isSwitchable() ? new SwitcherAdvice(advice) : advice;
    }

    public static Advice getOrCreate(Class<?> adviceClass) {
        return ADVICES.computeIfAbsent(adviceClass, clazz -> {
            try {
                return switcher((Advice) clazz.getConstructor().newInstance());
            } catch (Exception e) {
                throw new GravityException(e);
            }
        });
    }
}
