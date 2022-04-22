package io.manbang.gravity.plugin;

import java.util.logging.Logger;

/**
 * @author weilong.hu
 * @since 2020/12/1 15:57
 */
enum JULLogger implements GravityLogger {
    /**
     * jul logger
     */
    INSTANCE;

    @Override
    public void info(String logName, String msg) {
        Logger.getLogger(logName).info(() -> msg);
    }

    @Override
    public void info(String logName, String format, Object arg) {
        Logger.getLogger(logName).info(() -> String.format(format, arg));
    }

    @Override
    public void info(String logName, String format, Object arg1, Object arg2) {
        Logger.getLogger(logName).info(() -> String.format(format, arg1, arg2));
    }

    @Override
    public void info(String logName, String format, Object... arguments) {
        Logger.getLogger(logName).info(() -> String.format(format, arguments));
    }

    @Override
    public void info(String logName, String msg, Throwable t) {
        Logger.getLogger(logName).info(() -> String.format(msg, GravityUtils.getStackTrace(t)));
    }

    @Override
    public void warn(String logName, String msg) {
        Logger.getLogger(logName).warning(() -> msg);
    }

    @Override
    public void warn(String logName, String format, Object arg) {
        Logger.getLogger(logName).warning(() -> String.format(format, arg));
    }

    @Override
    public void warn(String logName, String format, Object arg1, Object arg2) {
        Logger.getLogger(logName).warning(() -> String.format(format, arg1, arg2));
    }

    @Override
    public void warn(String logName, String format, Object... arguments) {
        Logger.getLogger(logName).warning(() -> String.format(format, arguments));
    }

    @Override
    public void warn(String logName, String msg, Throwable t) {
        Logger.getLogger(logName).warning(() -> String.format(msg, GravityUtils.getStackTrace(t)));
    }

    @Override
    public void error(String logName, String msg) {
        Logger.getLogger(logName).warning(() -> msg);
    }

    @Override
    public void error(String logName, String format, Object arg) {
        Logger.getLogger(logName).warning(() -> String.format(format, arg));
    }

    @Override
    public void error(String logName, String format, Object arg1, Object arg2) {
        Logger.getLogger(logName).warning(() -> String.format(format, arg1, arg2));
    }

    @Override
    public void error(String logName, String format, Object... arguments) {
        Logger.getLogger(logName).warning(() -> String.format(format, arguments));
    }

    @Override
    public void error(String logName, String msg, Throwable t) {
        Logger.getLogger(logName).warning(() -> String.format("msg:%s exception:%s", msg, GravityUtils.getStackTrace(t)));
    }
}
