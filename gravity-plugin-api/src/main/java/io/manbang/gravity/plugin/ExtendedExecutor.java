package io.manbang.gravity.plugin;

/**
 * 扩展执行器 用于在目标环境下执行
 *
 * @author weilong.hu
 * @since 2021/09/03 18:10
 */
public interface ExtendedExecutor {
    default void execute(ExecuteContext context) {
    }
}
