package io.manbang.gravity.plugin;

/**
 * @author weilong.hu
 * @since 2020/12/1 15:17
 */
public interface GravityLogger {
    /**
     * 从SPI中获取一个logger
     *
     * @return 开关
     */
    static GravityLogger of(ClassLoader classLoader) {
        return CacheHolder.INSTANCE.loadIfAbsent(classLoader, GravityLogger.class, JULLogger.INSTANCE);
    }

    /**
     * 从SPI中获取一个logger
     *
     * @return 开关
     */
    static GravityLogger jul() {
        return JULLogger.INSTANCE;
    }

    void info(String logName, String msg);

    void info(String logName, String format, Object arg);

    void info(String logName, String format, Object arg1, Object arg2);

    void info(String logName, String format, Object... arguments);

    void info(String logName, String msg, Throwable t);

    void warn(String logName, String msg);

    void warn(String logName, String format, Object arg);

    void warn(String logName, String format, Object arg1, Object arg2);

    void warn(String logName, String format, Object... arguments);

    void warn(String logName, String msg, Throwable t);

    void error(String logName, String msg);

    void error(String logName, String format, Object arg);

    void error(String logName, String format, Object arg1, Object arg2);

    void error(String logName, String format, Object... arguments);

    void error(String logName, String msg, Throwable t);
}
