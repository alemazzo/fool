package compiler.operators;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AndTest {

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

}
