package io.manbang.gravity.plugin;

/**
 * 方法拦截器，拦截器是不能修改方法的body内容的，原方法会被包装成一个新的方法
 * <pre>
 *     // original class
 *     public class Foo {
 *         public String sayHello() {
 *             return "Hello Interceptor.";
 *         }
 *     }
 *
 *     // interceptor class
 *     public FooInterceptor implements Interceptor {
 *         public boolean beforeMethod(ExecuteContext context) {
 *             System.err.println("Hello Before Method.");
 *             return true;
 *         }
 *
 *         public void handleException(ExecuteContext context) {
 *             System.err.println("Hello Handle Exception.");
 *         }
 *
 *         public void afterMethod(ExecuteContext context) {
 *             System.err.println("Hello After Method.");
 *         }
 *
 *         public void onCompleted(ExecuteContext context) {
 *             System.err.println("Hello on Completed.);
 *         }
 *     }
 *
 *     // Foo final pseudocode class
 *     public class Foo {
 *         public String sayHello() {
 *             // ......
 *             ExecuteContext context = getContext();
 *             boolean continued = interceptor.beforeMethod(context);
 *             if (continued) {
 *                 try {
 *                     String result = sayHelloWrapped();
 *                     context.setResult(result);
 *                     interceptor.afterMethod(context);
 *                 } catch(Throwable throwable) {
 *                     context.setThrowable(throwable);
 *                     interceptor.handleException(context);
 *                 } finally {
 *                     interceptor.onCompleted(context);
 *                 }
 *             }
 *
 *             if (context.isRethrown()) {
 *                 throw context.getThrowable();
 *             }
 *
 *             return context.getResult();
 *         }
 *
 *         private String sayHelloWrapped() {
 *             return "Hello Interceptor.";
 *         }
 *     }
 * </pre>
 *
 * @author duoliang.zhang
 * @since 2020/8/22 11:27
 */
public interface Interceptor extends Named, LifeCycle, Switchable {

    /**
     * 将原始 {@link Interceptor} 做一次包装，如果 {@link Interceptor} 支持开关的话，会封装成 {@link SwitcherInterceptor}，否则直接返回
     *
     * @param interceptor 拦截器插件
     * @return 如果是可以开关控制的 {@link Interceptor}，返回包装后的 {@link SwitcherInterceptor}
     */
    static Interceptor switcher(Interceptor interceptor) {
        return interceptor.isSwitchable() ? new SwitcherInterceptor(interceptor) : interceptor;
    }

    /**
     * 原方法执行前，回调，可以通过 {@link ExecuteContext#setResult(Object)} 设置新的返回值
     *
     * @param context 执行上下文
     * @return 如果继续希望继续执行，返回<code>true</code>，否则，原方法不再执行
     */
    default boolean beforeMethod(ExecuteContext context) {
        return true;
    }

    /**
     * 如果原方法执行异常，回调此方法处理，{@link ExecuteContext#getThrowable()} 获取异常信息，默认直接抛出异常
     *
     * @param context 执行上下文
     */
    default void handleException(ExecuteContext context) throws Throwable {
        if (context.getThrowable() != null) {
            throw context.getThrowable();
        }
    }

    /**
     * 远方执行之后，回调，
     *
     * @param context 执行上下文
     */
    default void afterMethod(ExecuteContext context) {

    }

    /**
     * @param context 执行上下文
     */
    default void onCompleted(ExecuteContext context) {

    }
}
