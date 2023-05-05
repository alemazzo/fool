package compiler;

import org.junit.jupiter.api.Test;

import static compiler.CodeUtils.*;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EASTGenerationTest {

    private static final String BASE_CLASS_CODE = """
            let
                class Account (money:int) {
                    fun getMon:int () money;
                }
            in
                true;
            """;

    private static final String BASE_CLASS_CODE_WITH_TWO_FIELDS = """
            let 
                class Account (money:int, money2:int) {
                    fun getMon:int () money;
                }
            in
                true;
            """;

    private static final String BASE_CLASS_CODE_WITH_TWO_METHODS = """
            let 
                class Account (money:int) {
                    fun getMon:int () money;
                    fun getMon2:int () money;
                }
            in
                true;
            """;


    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    @Test
    void testClassDec() {
        final var progNode = getProgLetInNodeFromEAST(fromString(BASE_CLASS_CODE));
        assertEquals(1, progNode.decList.size());
        assertEquals(AST.ClassNode.class, progNode.decList.get(0).getClass());
    }

    @Test
    void testClassAttribute() {
        final var progNode = getProgLetInNodeFromEAST(fromString(BASE_CLASS_CODE));
        final var classNode = (AST.ClassNode) progNode.decList.get(0);
        assertEquals(1, classNode.fields.size());
        assertEquals(AST.FieldNode.class, classNode.fields.get(0).getClass());

        final AST.FieldNode fieldNode = classNode.fields.get(0);
        assertEquals("money", fieldNode.fieldId);
    }

    @Test
    void testClassMultipleFields() {
        final var progNode = getProgLetInNodeFromEAST(fromString(BASE_CLASS_CODE_WITH_TWO_FIELDS));
        final var classNode = (AST.ClassNode) progNode.decList.get(0);
        assertEquals(2, classNode.fields.size());
        assertEquals(AST.FieldNode.class, classNode.fields.get(0).getClass());
        assertEquals(AST.FieldNode.class, classNode.fields.get(1).getClass());

        final AST.FieldNode fieldNode = classNode.fields.get(0);
        assertEquals("money", fieldNode.fieldId);

        final AST.FieldNode fieldNode2 = classNode.fields.get(1);
        assertEquals("money2", fieldNode2.fieldId);
    }

    @Test
    void testClassMethods() {
        final var progNode = getProgLetInNodeFromEAST(fromString(BASE_CLASS_CODE));
        final var classNode = (AST.ClassNode) progNode.decList.get(0);
        assertEquals(1, classNode.methods.size());
        assertEquals(AST.MethodNode.class, classNode.methods.get(0).getClass());

        final AST.MethodNode methodNode = classNode.methods.get(0);
        assertEquals("getMon", methodNode.methodId);
    }

    @Test
    void testClassMultipleMethods() {
        final var progNode = getProgLetInNodeFromEAST(fromString(BASE_CLASS_CODE_WITH_TWO_METHODS));
        final var classNode = (AST.ClassNode) progNode.decList.get(0);
        assertEquals(2, classNode.methods.size());
        assertEquals(AST.MethodNode.class, classNode.methods.get(0).getClass());
        assertEquals(AST.MethodNode.class, classNode.methods.get(1).getClass());

        final AST.MethodNode methodNode = classNode.methods.get(0);
        assertEquals("getMon", methodNode.methodId);

        final AST.MethodNode methodNode2 = classNode.methods.get(1);
        assertEquals("getMon2", methodNode2.methodId);
    }

    @Test
    void testClassWithSuperclass() {
        final var code = """
                let
                    class Account (money:int) {
                        fun getMon:int () money;
                    }
                    class TradingAcc extends Account (invested:int) {
                        fun getInv:int () invested;
                    }
                in
                    true;
                """;
        final var progNode = getProgLetInNodeFromEAST(fromString(code));
        assertEquals(2, progNode.decList.size());

        final var classNode = (AST.ClassNode) progNode.decList.get(0);
        final var classNode2 = (AST.ClassNode) progNode.decList.get(1);

        assertTrue(classNode2.superId.isPresent());
        assertEquals("Account", classNode2.superId.get());

        assertEquals(1, classNode.fields.size());
        assertEquals(1, classNode2.fields.size());
    }


    @Test
    void testClassWithSuperclassThatOverrideFieldAndMethod() {
        final var code = """
                let
                    class Account (money:int) {
                        fun getMon:int () money;
                    }
                    class TradingAcc extends Account (money:int) {
                        fun getMon:int () money;
                    }
                in
                    true;
                """;
        final var progNode = getProgLetInNodeFromEAST(fromString(code));
        assertEquals(2, progNode.decList.size());

        final var classNode = (AST.ClassNode) progNode.decList.get(0);
        final var classNode2 = (AST.ClassNode) progNode.decList.get(1);

        assertTrue(classNode2.superId.isPresent());
        assertEquals("Account", classNode2.superId.get());

        assertEquals(1, classNode.fields.size());
        assertEquals(1, classNode2.fields.size());
        assertEquals("money", classNode.fields.get(0).fieldId);
        assertEquals("money", classNode2.fields.get(0).fieldId);

        assertEquals(1, classNode.methods.size());
        assertEquals(1, classNode2.methods.size());
        assertEquals("getMon", classNode.methods.get(0).methodId);
        assertEquals("getMon", classNode2.methods.get(0).methodId);

    }

    @Test
    void testClassWithSuperclassWithFieldThatOverrideMethod() {
        final var code = """
                let
                    class Account (money:int) {
                        fun getMon:int () money;
                    }
                    class TradingAcc extends Account (money:int, getMon:int) {
                    
                    }
                in
                    true;
                """;
        interceptOutput();
        final var progNode = getProgLetInNodeFromEAST(fromString(code));
        final var output = getOutput();
        assertEquals("Cannot override method getMon with a field", output);
    }

}
