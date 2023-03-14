package compiler;

import org.junit.jupiter.api.Test;

import static compiler.CodeTester.getOutputFromSourceCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeTests {

    @Test
    void testMinus() {
        var code = "print(5 - 2);";
        var output = getOutputFromSourceCode(code);
        assertEquals(3, Integer.parseInt(output));
    }

}
