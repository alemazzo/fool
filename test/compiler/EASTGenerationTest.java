package compiler;

import compiler.AST.BoolNode;
import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.getProgNodeFromEAST;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EASTGenerationTest {

    @Test
    void testExample() {
        final var code = "true;";
        final var progNode = getProgNodeFromEAST(fromString(code));
        assertEquals(BoolNode.class, progNode.exp.getClass());
        // ACCESS STEntry
    }


    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************
    @Test
    void testIsClass(){
        final var code = "let class Account (money:int) { fun getMon:int () money;} in true;";
        final var progNode = getProgNodeFromEAST(fromString(code));
        assertEquals(AST.ClassTypeNode.class, progNode.exp.getClass());
    }


}
