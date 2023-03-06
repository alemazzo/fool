package compiler;

import compiler.lib.Node;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SymbolTableASTVisitorTest {
    private Node ast;
    private SymbolTableASTVisitor symbolTableASTVisitor;

    private Node createAST() throws IOException {
        final String fileName = "examples/prova.fool";
        final CharStream chars = CharStreams.fromFileName(fileName);
        final FOOLLexer lexer = new FOOLLexer(chars);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final FOOLParser parser = new FOOLParser(tokens);
        final ASTGenerationSTVisitor astGenerationSTVisitor = new ASTGenerationSTVisitor();
        return astGenerationSTVisitor.visit(parser.prog());
    }

    @BeforeEach
    void setUp() throws IOException {
        this.ast = createAST();
        this.symbolTableASTVisitor = new SymbolTableASTVisitor();
    }


    @Test
    void testZeroSymbolTableErrors() {
        symbolTableASTVisitor.visit(ast);
        assertEquals(0, symbolTableASTVisitor.stErrors);
    }

}
