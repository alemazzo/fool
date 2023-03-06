package compiler;

import compiler.exc.TypeException;
import compiler.lib.FOOLlib;
import compiler.lib.Node;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeCheckEASTVisitorTest {
    private Node east;
    private TypeCheckEASTVisitor typeCheckVisitor;

    private Node createEAST() throws IOException {
        final String fileName = "examples/prova.fool";
        final CharStream chars = CharStreams.fromFileName(fileName);
        final FOOLLexer lexer = new FOOLLexer(chars);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final FOOLParser parser = new FOOLParser(tokens);
        final ASTGenerationSTVisitor astGenerationSTVisitor = new ASTGenerationSTVisitor();
        final Node ast = astGenerationSTVisitor.visit(parser.prog());
        final SymbolTableASTVisitor symbolTableASTVisitor = new SymbolTableASTVisitor();
        symbolTableASTVisitor.visit(ast);
        return ast;
    }

    @BeforeEach
    void setUp() throws IOException {
        this.east = createEAST();
        this.typeCheckVisitor = new TypeCheckEASTVisitor();
    }


    @Test
    void testZeroTypeCheckErrors() throws TypeException {
        typeCheckVisitor.visit(east);
        assertEquals(0, FOOLlib.typeErrors);
    }

}
