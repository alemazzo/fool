package compiler;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OperatorsTest {

    @Test
    void testNotOnFalse() {
        final var code = "print(!false);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testNotOnTrue() {
        final var code = "print(!true);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }

    @Test
    void testDoubleNotOnFalse() {
        final var code = "print(!!false);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }

    @Test
    void testDoubleNotOnTrue() {
        final var code = "print(!!true);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testOrOnTrueAndTrue() {
        final var code = "print(true || true);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testOrOnTrueAndFalse() {
        final var code = "print(true || false);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testOrOnFalseAndTrue() {
        final var code = "print(false || true);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testOrOnFalseAndFalse() {
        final var code = "print(false || false);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }
    
    @Test
    void testAndOnTrueAndTrue() {
        final var code = "print(true && true);";
        final var result = getOutput(fromString(code));
        assertEquals(1, parseInt(result));
    }

    @Test
    void testAndOnTrueAndFalse() {
        final var code = "print(true && false);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }

    @Test
    void testAndOnFalseAndTrue() {
        final var code = "print(false && true);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }

    @Test
    void testAndOnFalseAndFalse() {
        final var code = "print(false && false);";
        final var result = getOutput(fromString(code));
        assertEquals(0, parseInt(result));
    }

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
