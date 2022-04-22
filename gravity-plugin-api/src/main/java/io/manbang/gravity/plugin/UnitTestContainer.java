package io.manbang.gravity.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * 为插件提供UT支持，思路为：植入代码负责写，Test Case负责读
 * <p>
 * - agentRunning: 表示PluginAgent是否正常启动
 * <p>
 * - feature: 表示UT功能是否启用
 * <p>
 * 每个key对应的value是一个queue，用于检测植入的代码是否只执行了1次（非常重要!）
 *
 * @author Kai
 */
public class UnitTestContainer {
    /**
     * UT测试功能标识
     */
    private static volatile boolean feature = false;

    /**
     * 开启UT测试
     */
    public static void enableUnitTest() {
        feature = true;
    }

    /**
     * 判断UT测试是否开启
     */
    public static boolean isUnitTestEnabled() {
        return feature;
    }


    /**
     * agent运行标记
     */
    private static volatile boolean agentRunning = false;

    /**
     * 开启UT测试
     */
    public static void markAgentRunning() {
        agentRunning = true;
    }

    /**
     * 判断UT测试是否开启
     */
    public static boolean isAgentRunning() {
        return agentRunning;
    }

    /**
     * UT使用的KV存储
     */
    private static ThreadLocal<Map<String, Queue<String>>> keyValueHolder = new ThreadLocal<>();

    public static String peek(String key) {
        return getQueue(key).peek();
    }

    public static int size(String key) {
        return getQueue(key).size();
    }

    public static String poll(String key) {
        return getQueue(key).poll();
    }

    public static void set(String key, String value) {
        add(key, value);
    }

    public static void add(String key, String value) {
        getQueue(key).add(value);
    }

    private static Queue<String> getQueue(String key) {
        Map<String, Queue<String>> map = getContainer();

        return map.computeIfAbsent(key, k -> new LinkedList<>());
    }

    private static Map<String, Queue<String>> getContainer() {
        Map<String, Queue<String>> map = keyValueHolder.get();
        if (map == null) {
            map = new HashMap<>();
            keyValueHolder.set(map);
        }

        return map;
    }

    public static void clear() {
        keyValueHolder.remove();
    }
}
