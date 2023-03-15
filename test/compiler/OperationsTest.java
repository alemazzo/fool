package compiler;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OperationsTest {

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


    @Test
    void testPlus() {
        final var code = "print(1 + 2);";
        final var result = getOutput(fromString(code));
        assertEquals(3, parseInt(result));
    }

    @Test
    void testPlusWithNegative() {
        final var code = "print(1 + -2);";
        final var result = getOutput(fromString(code));
        assertEquals(-1, parseInt(result));
    }

    @Test
    void testPlusWithNegativeAndMinus() {
        final var code = "print(-1 + -2);";
        final var result = getOutput(fromString(code));
        assertEquals(-3, parseInt(result));
    }

    @Test
    void testPlusWithNegativeAndMinusAndPlus() {
        final var code = "print(-1 + -2 + 3);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }


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
