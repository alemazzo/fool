package compiler;

import compiler.exc.TypeException;
import compiler.lib.Node;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CodeGenerationEASTVisitorTest {
    private Node east;

    private CodeGenerationASTVisitor codeGenerationASTVisitor;

    private Node createEAST() throws IOException, TypeException {
        final String fileName = "examples/prova.fool";
        final CharStream chars = CharStreams.fromFileName(fileName);
        final FOOLLexer lexer = new FOOLLexer(chars);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final FOOLParser parser = new FOOLParser(tokens);
        final ASTGenerationSTVisitor astGenerationSTVisitor = new ASTGenerationSTVisitor();
        final Node ast = astGenerationSTVisitor.visit(parser.prog());
        final SymbolTableASTVisitor symbolTableASTVisitor = new SymbolTableASTVisitor();
        symbolTableASTVisitor.visit(ast);
        final TypeCheckEASTVisitor typeCheckVisitor = new TypeCheckEASTVisitor();
        typeCheckVisitor.visit(ast);
        return ast;
    }

    @BeforeEach
    void setUp() throws IOException, TypeException {
        this.east = createEAST();
        this.codeGenerationASTVisitor = new CodeGenerationASTVisitor();
    }


    @Test
    void testGenerateCode() {
        final String code = codeGenerationASTVisitor.visit(east);
        assertTrue(code.length() > 0);
    }

}
