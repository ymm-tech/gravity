package io.manbang.gravity.plugin;

/**
 * 切面，这里的回调会直接植入到目标方法的方法体前后
 * <pre>
 *     // original class
 *     public class Foo {
 *         public void sayHello() {
 *             System.out.println("Hello Advice);
 *         }
 *     }
 *
 *     // advice class
 *     public class FooAdvice {
 *         public void enterMethod(ExecuteContext context) {
 *              System.err.println("Hello Enter Method.");
 *         }
 *
 *         public void exitMethod(ExecuteContext context) {
 *             System.err.println("Hello Exit Method.);
 *         }
 *     }
 *
 *     // Foo final pseudocode class
 *     public class Foo {
 *         public void sayHello() {
 *             System.err.println("Hello Enter Method.");
 *             System.out.println("Hello Advice);
 *             System.err.println("Hello Exit Method.);
 *         }
 *     }
 * </pre>
 *
 * @author duoliang.zhang
 * @since 2020/8/22 11:25
 */
public interface Advice extends Named, LifeCycle, Switchable {

    static io.manbang.gravity.plugin.Advice of(Class<?> adviceClass) {
        return AdviceHolder.getOrCreate(adviceClass);
    }

    /**
     * 方法体前执行逻辑，如果想提前终止业务方法执行的话，可以执行 {@link ExecuteContext#skip()}
     * 想要改变返回值的话，在 {@link #exitMethod(ExecuteContext)} 中设置自定义的返回值
     *
     * @param context 执行上下文
     */
    default void enterMethod(ExecuteContext context) {
    }

    /**
     * 方法提后逻辑
     *
     * @param context 执行上下文
     */
    default void exitMethod(ExecuteContext context) {
    }
}
