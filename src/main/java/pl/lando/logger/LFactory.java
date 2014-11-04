package pl.lando.logger;

public class LFactory {
    public static L create(L.LoggerCallback callback) {
        return new L(callback, new StringEvaluator());
    }
}
