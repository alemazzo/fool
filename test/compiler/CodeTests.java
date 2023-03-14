package compiler;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;

import static compiler.CodeTester.getOutput;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeTests {

    @Test
    void testMinus() {
        var code = "print(5 - 2);";
        var output = getOutput(CharStreams.fromString(code));
        assertEquals(3, Integer.parseInt(output));
    }

}
