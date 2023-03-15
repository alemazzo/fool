package compiler.operators;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static java.lang.Integer.parseInt;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotTest {

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
    
}
