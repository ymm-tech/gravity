package io.manbang.gravity.plugin;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 智能拦截器，可以监听是否启用，为了防止每次调用开关量的判断，会直接监听开关的变化，然后更换实际执行的拦截器，如此可以节省每次判断开关量的开销
 *
 * @author duoliang.zhang
 * @since 2020/11/5 10:49
 */
class SwitcherInterceptor implements Interceptor, SwitcherListener {
    private final AtomicReference<Interceptor> interceptorRef;
    private final Interceptor interceptor;

    SwitcherInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
        this.interceptorRef = new AtomicReference<>(EmptyInterceptor.INSTANCE);
        this.subscribeSwitcherChange(interceptor);
    }

    private void subscribeSwitcherChange(Interceptor interceptor) {
        Switcher.of(interceptor.getClass().getClassLoader()).addListener(this);
    }

    @Override
    public boolean beforeMethod(ExecuteContext context) {
        try {
            return interceptorRef.get().beforeMethod(context);
        } catch (Exception e) {
            GravityLogger.of(interceptor.getClass().getClassLoader()).error("G:Interceptor", "重力方法前置回调异常，不影响业务，如出现次数较多，可联系gravity负责人定位。", e);
            return true;
        }
    }

    @Override
    public void handleException(ExecuteContext context) throws Throwable {
        interceptorRef.get().handleException(context);
    }

    @Override
    public void afterMethod(ExecuteContext context) {
        try {
            interceptorRef.get().afterMethod(context);
        } catch (Exception e) {
            GravityLogger.of(interceptor.getClass().getClassLoader()).error("G:Interceptor", "重力方法后置回调异常，不影响业务，如出现次数较多，可联系gravity负责人定位。", e);
        }
    }

    @Override
    public void onCompleted(ExecuteContext context) {
        try {
            interceptorRef.get().onCompleted(context);
        } catch (Exception e) {
            GravityLogger.of(interceptor.getClass().getClassLoader()).error("G:Interceptor", "重力方法完成回调异常，不影响业务，如出现次数较多，可联系gravity负责人定位。", e);
        }
    }

    @Override
    public String getName() {
        return interceptor.getName();
    }

    @Override
    public void on() {
        interceptorRef.set(interceptor);
        interceptor.resume();
    }

    @Override
    public void off() {
        interceptorRef.set(EmptyInterceptor.INSTANCE);
        interceptor.suspend();
    }
}
