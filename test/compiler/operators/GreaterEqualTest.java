package compiler.operators;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GreaterEqualTest {

    @Test
    void testGreaterEqual() {
        final var code = "print(10 >= 2);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testGreaterEqualWithEqual() {
        final var code = "print(10 >= 10);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testGreaterEqualWithLess() {
        final var code = "print(10 >= 20);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }

    @Test
    void testGreaterEqualWithNegatives() {
        final var code = "print(-10 >= -20);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testGreaterEqualWithNegativesAndEqual() {
        final var code = "print(-10 >= -10);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testGreaterEqualWithNegativesAndLess() {
        final var code = "print(-10 >= -5);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }

}
