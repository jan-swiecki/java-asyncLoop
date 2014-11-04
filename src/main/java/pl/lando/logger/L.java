package pl.lando.logger;

import java.util.ArrayList;

public class L {

    private final StringEvaluator evaluator;

    @FunctionalInterface
    public static interface LoggerCallback {
        public void apply(String message);
    }

    public static L instance(LoggerCallback callback) {
        return LFactory.create(callback);
    }
    public static L sout() {
        return LFactory.create(System.out::println);
    }

    private final LoggerCallback callback;

    public L(LoggerCallback callback, StringEvaluator evaluator) {
        this.callback = callback;
        this.evaluator = evaluator;
    }

    public void d(String message, Object... values) {
        callback.apply("[DEBUG] "+ev(message, values));
    }

    public void debug(String message, Object... values) {
        d(message, values);
    }

    public void e(String message, Object... values) {
        callback.apply("[ERROR] "+ev(message, values));
    }

    public void error(String message, Object... values) {
        d(message, values);
    }

    public void w(String message, Object... values) {
        callback.apply("[WARN] "+ev(message, values));
    }

    public void warn(String message, Object... values) {
        d(message, values);
    }

    private String ev(String message, Object... values) {
        return evaluator.evaluate(message, values);
    }
}
