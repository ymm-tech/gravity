package io.manbang.gravity.plugin;

/**
 * @author duoliang.zhang
 * @since 2021/8/26 17:46
 */
public class Constants {
    public static final String EXIT_METHOD_ERROR = "重力退出方法回调异常，不影响业务，如出现次数较多，可联系gravity负责人定位。";
    public static final String ENTER_METHOD_ERROR = "重力进入方法回调异常，不影响业务，如出现次数较多，可联系gravity负责人定位。";
    public static final String ADVICE_LOGGER_NAME = "G:Advice";

    private Constants() {
        throw new UnsupportedOperationException();
    }
}
