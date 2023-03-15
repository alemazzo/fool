package compiler;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getOutput;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeTests {

    @Test
    void testMinus() {
        var code = "print(5 - 2);";
        var output = getOutput(CharStreams.fromString(code));
        assertEquals(3, Integer.parseInt(output));
    }

    @Test
    void testNot() {
        var code = "print(!false);";
        var output = getOutput(CharStreams.fromString(code));
        assertEquals(1, Integer.parseInt(output));

        code = "print(!true);";
        output = getOutput(CharStreams.fromString(code));
        assertEquals(0, Integer.parseInt(output));

        code = "print(!(!true));";
        output = getOutput(CharStreams.fromString(code));
        assertEquals(1, Integer.parseInt(output));
    }

    @Test
    void testAnd() {
        var code = "print(true && false);";
        var output = getOutput(CharStreams.fromString(code));
        assertEquals(0, Integer.parseInt(output));
    }

    @Test
    void testOr() {
        var code = "print(true || false);";
        var output = getOutput(CharStreams.fromString(code));
        assertEquals(1, Integer.parseInt(output));
    }

    @Test
    void testDiv() {
        var code = "print(10 / 2);";
        var output = getOutput(CharStreams.fromString(code));
        assertEquals(5, Integer.parseInt(output));
    }

    @Test
    void testGreaterEqual() {
        var code = "print(10 >= 2);";
        var output = getOutput(CharStreams.fromString(code));
        assertEquals(1, Integer.parseInt(output));
    }

    @Test
    void testLessEqual() {
        var code = "print(10 <= 2);";
        var output = getOutput(CharStreams.fromString(code));
        assertEquals(0, Integer.parseInt(output));
    }


}
