package compiler;

import compiler.AST.*;

import static compiler.CodeUtils.*;
import static org.antlr.v4.runtime.CharStreams.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ASTGenerationTest {

    @Test
    void testProgNode() {
        final var progCode = "print(1);";
        final var ast = getAST(fromString(progCode));
        assertEquals(ProgNode.class, ast.getClass());
    }

    @Test
    void testProgLetInNode() {
        final var progLetInCode = "let var x:int = 1; in print(x);";
        final var ast = getAST(fromString(progLetInCode));
        assertEquals(ProgLetInNode.class, ast.getClass());
    }

    @Test
    void testFunNode() {
        final var funCode = "let fun f:int (x:int) = x; in print(f(1));";
        final ProgLetInNode progLetInNode = getProgLetInNodeFromAST(fromString(funCode));

        assertEquals(1, progLetInNode.declarations.size());
        assertEquals(FunNode.class, progLetInNode.declarations.get(0).getClass());
    }

    @Test
    void testParNode() {
        final var parCode = "let fun f:int (x:int) = x; in print(f(1));";
        final ProgLetInNode progLetInNode = getProgLetInNodeFromAST(fromString(parCode));

        assertEquals(1, progLetInNode.declarations.size());
        assertEquals(FunNode.class, progLetInNode.declarations.get(0).getClass());

        final FunNode funNode = (FunNode) progLetInNode.declarations.get(0);
        assertEquals(1, funNode.parameters.size());
        assertEquals(ParNode.class, funNode.parameters.get(0).getClass());
        assertEquals("x", funNode.parameters.get(0).id);
    }

    @Test
    void testVarNode() {
        final var varCode = "let var x:int = 1; in print(x);";
        final ProgLetInNode progLetInNode = getProgLetInNodeFromAST(fromString(varCode));
        assertEquals(1, progLetInNode.declarations.size());
        assertEquals(VarNode.class, progLetInNode.declarations.get(0).getClass());

        final VarNode varNode = (VarNode) progLetInNode.declarations.get(0);
        assertEquals("x", varNode.id);
    }

    @Test
    void testPrintNode() {
        final var printCode = "print(1);";
        final var progNode = getProgNodeFromAST(fromString(printCode));
        assertEquals(PrintNode.class, progNode.exp.getClass());
    }

    @Test
    void testIfNode() {
        final var ifCode = "if 1 then 2 else 3;";
        final var progNode = getProgNodeFromAST(fromString(ifCode));
        assertEquals(IfNode.class, progNode.exp.getClass());
    }

    @Test
    void testEqualNode() {
        final var equalCode = "1 == 2;";
        final var progNode = getProgNodeFromAST(fromString(equalCode));
        assertEquals(EqualNode.class, progNode.exp.getClass());
    }

    @Test
    void testIdNode() {
        final var idCode = "let var x:int = 1; in print(x);";
        final ProgLetInNode progLetInNode = getProgLetInNodeFromAST(fromString(idCode));
        assertEquals(1, progLetInNode.declarations.size());
        assertEquals(VarNode.class, progLetInNode.declarations.get(0).getClass());

        final VarNode varNode = (VarNode) progLetInNode.declarations.get(0);
        assertEquals("x", varNode.id);

        final PrintNode printNode = (PrintNode) progLetInNode.exp;
        assertEquals(IdNode.class, printNode.exp.getClass());

        final IdNode idNode = (IdNode) printNode.exp;
        assertEquals("x", idNode.id);
    }

    @Test
    void testBoolNode() {
        final var boolCode = "true;";
        final var progNode = getProgNodeFromAST(fromString(boolCode));
        assertEquals(BoolNode.class, progNode.exp.getClass());

        final BoolNode boolNode = (BoolNode) progNode.exp;
        assertEquals(true, boolNode.value);
    }

    @Test
    void testIntNode() {
        final var intCode = "1;";
        final var progNode = getProgNodeFromAST(fromString(intCode));
        assertEquals(IntNode.class, progNode.exp.getClass());

        final IntNode intNode = (IntNode) progNode.exp;
        assertEquals(1, intNode.value);
    }

    @Test
    void testCallNode() {
        final var callCode = "let fun f:int (x:int) = x; in print(f(1));";
        final ProgLetInNode progLetInNode = getProgLetInNodeFromAST(fromString(callCode));
        assertEquals(PrintNode.class, progLetInNode.exp.getClass());

        final PrintNode printNode = (PrintNode) progLetInNode.exp;
        assertEquals(CallNode.class, printNode.exp.getClass());

        final CallNode callNode = (CallNode) printNode.exp;
        assertEquals("f", callNode.id);
        assertEquals(1, callNode.arguments.size());
    }

    @Test
    void testGreaterEqualNode() {
        final var greaterEqualCode = "1 >= 2;";
        final var progNode = getProgNodeFromAST(fromString(greaterEqualCode));
        assertEquals(GreaterEqualNode.class, progNode.exp.getClass());
    }

    @Test
    void testLessEqualNode() {
        final var lessEqualCode = "1 <= 2;";
        final var progNode = getProgNodeFromAST(fromString(lessEqualCode));
        assertEquals(LessEqualNode.class, progNode.exp.getClass());
    }

    @Test
    void testNotNode() {
        final var notCode = "!true;";
        final var progNode = getProgNodeFromAST(fromString(notCode));
        assertEquals(NotNode.class, progNode.exp.getClass());
    }

    @Test
    void testOrNode() {
        final var orCode = "true || false;";
        final var progNode = getProgNodeFromAST(fromString(orCode));
        assertEquals(OrNode.class, progNode.exp.getClass());
    }

    @Test
    void testAndNode() {
        final var andCode = "true && false;";
        final var progNode = getProgNodeFromAST(fromString(andCode));
        assertEquals(AndNode.class, progNode.exp.getClass());
    }

    @Test
    void testTimesNode() {
        final var timesCode = "1 * 2;";
        final var progNode = getProgNodeFromAST(fromString(timesCode));
        assertEquals(TimesNode.class, progNode.exp.getClass());
    }

    @Test
    void testDivNode() {
        final var divCode = "1 / 2;";
        final var progNode = getProgNodeFromAST(fromString(divCode));
        assertEquals(DivNode.class, progNode.exp.getClass());
    }

    @Test
    void testPlusNode() {
        final var plusCode = "1 + 2;";
        final var progNode = getProgNodeFromAST(fromString(plusCode));
        assertEquals(PlusNode.class, progNode.exp.getClass());
    }

    @Test
    void testMinusNode() {
        final var minusCode = "1 - 2;";
        final var progNode = getProgNodeFromAST(fromString(minusCode));
        assertEquals(MinusNode.class, progNode.exp.getClass());
    }

    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************


}
