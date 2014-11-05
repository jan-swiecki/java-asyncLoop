package pl.lando.logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class StringEvaluator {
    public String evaluate(String message, Object... values) {
        if(values.length == 0) {
            return message;
        } else {
            return evaluate(message.replaceFirst("\\{\\}", toString(values[0])), ArrayUtils.remove(values, 0));
        }
    }

    public String toString(Object x) {
        if(x == null) {
            return "null";
        } else if(x instanceof Throwable) {
            return ExceptionUtils.getStackTrace((Throwable)x).replaceAll("\\$", "\\\\\\$");
        } else {
            return x.toString();
        }
    }
}
