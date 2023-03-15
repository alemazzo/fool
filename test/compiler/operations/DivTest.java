package compiler.operations;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DivTest {
    @Test
    void testDiv() {
        final var code = "print(1 / 2);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }

    @Test
    void testDivWithNegative() {
        final var code = "print(6 / -2);";
        final var result = getOutput(fromString(code));
        assertEquals(-3, parseInt(result));
    }

    @Test
    void testDivWithNegativeAndMinus() {
        final var code = "print(-10 / -2);";
        final var result = getOutput(fromString(code));
        assertEquals(5, parseInt(result));
    }

    @Test
    void testDivWithNegativeAndMinusAndDiv() {
        final var code = "print(-10 / -2 / 2);";
        final var result = getOutput(fromString(code));
        assertEquals(2, parseInt(result));
    }

}