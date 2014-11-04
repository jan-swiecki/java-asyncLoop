package pl.lando.logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

public class StringEvaluator {
    public String evaluate(String message, Object... values) {
        if(values.length == 0) {
            return message;
        } else {
            return evaluate(message.replaceFirst("\\{\\}", values[0].toString()), ArrayUtils.remove(values, 0));
        }
    }

}
