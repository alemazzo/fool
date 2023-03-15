package compiler.operators;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrTest {

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
    
}
