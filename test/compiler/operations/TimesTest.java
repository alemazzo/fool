package compiler.operations;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TimesTest {
    @Test
    void testTimes() {
        final var code = "print(1 * 2);";
        final var result = getOutput(fromString(code));
        assertEquals(2, parseInt(result));
    }

    @Test
    void testTimesWithNegative() {
        final var code = "print(1 * -2);";
        final var result = getOutput(fromString(code));
        assertEquals(-2, parseInt(result));
    }

    @Test
    void testTimesWithNegativeAndMinus() {
        final var code = "print(-1 * -2);";
        final var result = getOutput(fromString(code));
        assertEquals(2, parseInt(result));
    }

    @Test
    void testTimesWithNegativeAndMinusAndTimes() {
        final var code = "print(-1 * -2 * 3);";
        final var result = getOutput(fromString(code));
        assertEquals(6, parseInt(result));
    }

}