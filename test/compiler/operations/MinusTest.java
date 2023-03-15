package compiler.operations;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MinusTest {
    @Test
    void testMinus() {
        final var code = "print(1 - 2);";
        final var result = getOutput(fromString(code));
        assertEquals(-1, parseInt(result));
    }

    @Test
    void testMinusWithNegative() {
        final var code = "print(1 - -2);";
        final var result = getOutput(fromString(code));
        assertEquals(3, parseInt(result));
    }

    @Test
    void testMinusWithNegativeAndMinus() {
        final var code = "print(-1 - -2);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

}