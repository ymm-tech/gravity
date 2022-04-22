package io.manbang.gravity.plugin;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 智能切面，可以监听是否启用，为了防止每次调用开关量的判断，会直接监听开关的变化，然后更换实际执行的切面，如此可以节省每次判断开关量的开销
 *
 * @author duoliang.zhang
 * @since 2020/11/5 10:22
 */
class SwitcherAdvice implements Advice, SwitcherListener {
    private final AtomicReference<Advice> adviceRef;
    private final Advice advice;

    SwitcherAdvice(Advice advice) {
        this.advice = advice;
        this.adviceRef = new AtomicReference<>(EmptyAdvice.INSTANCE);
        this.subscribeSwitcherChange(advice);
    }

    private void subscribeSwitcherChange(Advice advice) {
        Switcher.of(advice.getClass().getClassLoader()).addListener(this);
    }

    @Override
    public void enterMethod(ExecuteContext context) {
        adviceRef.get().enterMethod(context);
    }

    @Override
    public void exitMethod(ExecuteContext context) {
        adviceRef.get().exitMethod(context);
    }

    @Override
    public String getName() {
        return advice.getName();
    }

    @Override
    public void on() {
        adviceRef.set(advice);
        advice.resume();
    }

    @Override
    public void off() {
        adviceRef.set(EmptyAdvice.INSTANCE);
        advice.suspend();
    }
}
