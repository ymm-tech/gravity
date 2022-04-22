package io.manbang.gravity.plugin;

/**
 * 重力异常
 *
 * @author duoliang.zhang
 * @since 2020/8/28 15:42
 */
public class GravityException extends RuntimeException {
    private static final long serialVersionUID = -8891459650895985754L;

    public GravityException() {
        super();
    }

    public GravityException(String message) {
        super(message);
    }

    public GravityException(String message, Throwable cause) {
        super(message, cause);
    }

    public GravityException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (isWritableStackTrace()) {
            return super.fillInStackTrace();
        }

        return this;
    }

    protected boolean isWritableStackTrace() {
        return false;
    }
}
