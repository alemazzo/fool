package compiler;

import compiler.lib.Node;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ASTGenerationVisitorTest {

    private ParseTree parseTree;
    private ASTGenerationSTVisitor visitor;


    private ParseTree createParseTree() throws IOException {
        final String fileName = "examples/prova.fool";
        final CharStream chars = CharStreams.fromFileName(fileName);
        final FOOLLexer lexer = new FOOLLexer(chars);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final FOOLParser parser = new FOOLParser(tokens);
        return parser.prog();
    }

    @BeforeEach
    void setUp() throws IOException {
        this.parseTree = createParseTree();
        this.visitor = new ASTGenerationSTVisitor();
    }


    @Test
    void testASTGeneration() {
        final Node ast = visitor.visit(parseTree);
        assertEquals(AST.ProgLetInNode.class, ast.getClass());
    }

}
