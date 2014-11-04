package pl.lando.logger;

import java.util.ArrayList;

public class L {

    @FunctionalInterface
    public static interface LoggerCallback {
        public void apply(String message);
    }

    private final LoggerCallback callback;

    public L(LoggerCallback callback) {
        this.callback = callback;
    }

    public static L instance(LoggerCallback callback) {
        return new L(callback);
    }

    public void d(String message, Object... values) {
        callback.apply("[DEBUG] "+message);
    }

    public void debug(String message, Object... values) {
        d(message, values);
    }

    public void e(String message, Object... values) {
        callback.apply("[ERROR] "+message);
    }

    public void error(String message, Object... values) {
        d(message, values);
    }

    public void w(String message, Object... values) {
        callback.apply("[WARN] "+message);
    }

    public void warn(String message, Object... values) {
        d(message, values);
    }

}
