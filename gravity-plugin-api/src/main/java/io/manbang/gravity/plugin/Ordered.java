package io.manbang.gravity.plugin;

/**
 * 排序
 *
 * @author duoliang.zhang
 * @since 2020/9/10 16:46
 */
public interface Ordered {
    /**
     * 获取顺序，值越小，排名越靠前，默认都是 0
     *
     * @return 顺序值
     */
    default int getOrder() {
        return 0;
    }
}
