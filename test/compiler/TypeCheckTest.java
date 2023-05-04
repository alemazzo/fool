package compiler;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.checkTypes;
import static compiler.CodeUtils.getEAST;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TypeCheckTest {

    @Test
    void testExample() {
        final var code = " let class Account (money:int) {" +
                "    fun getMon:int () money;" +
                "  } var me:Account = new Account(10);" +
                " in true;";
        final var east = getEAST(fromString(code));

        // Check types calling CodeUtils.checkTypes
        assertDoesNotThrow(() -> checkTypes(east));
    }


    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************


}
