package pl.lando.logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringEvaluatorTest {

    @Test
    public void evaluateOneTest() {
        StringEvaluator evaluator = new StringEvaluator();
        assertEquals("x", evaluator.evaluate("x"));
        assertEquals("xy", evaluator.evaluate("x{}", "y"));
        assertEquals("xyz", evaluator.evaluate("x{}{}", "y", "z"));
        assertEquals("xy{}z", evaluator.evaluate("x{}{}z", "y"));
        assertEquals("a = 5", evaluator.evaluate("a = {}", "5"));
        assertEquals("a = 5", evaluator.evaluate("{} = {}", "a", "5"));
        assertEquals("a = 5, b = 7", evaluator.evaluate("{} = {}, {} = {}", "a", "5", "b", "7"));
        assertEquals("a = 5, b = {}", evaluator.evaluate("{} = {}, {} = {}", "a", "5", "b"));
    }

}
