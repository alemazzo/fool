package compiler.operators;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LessEqualTest {

    @Test
    void testLessEqualFalse() {
        var code = "print(10 <= 2);";
        var output = getOutput(fromString(code));
        assertEquals(0, parseInt(output));
    }

    @Test
    void testLessEqualWithEqual() {
        var code = "print(10 <= 10);";
        var output = getOutput(fromString(code));
        assertEquals(1, parseInt(output));
    }

    @Test
    void testLessEqualWithGreater() {
        var code = "print(10 <= 20);";
        var output = getOutput(fromString(code));
        assertEquals(1, parseInt(output));
    }

    @Test
    void testLessEqualWithNegatives() {
        var code = "print(-10 <= -20);";
        var output = getOutput(fromString(code));
        assertEquals(0, parseInt(output));
    }

    @Test
    void testLessEqualWithNegativesAndEqual() {
        var code = "print(-10 <= -10);";
        var output = getOutput(fromString(code));
        assertEquals(1, parseInt(output));
    }

}
