package compiler;

import compiler.exc.TypeException;
import compiler.lib.Node;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import svm.SVMLexer;
import svm.SVMParser;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeAssemblyTest {
    private String code;

    private SVMParser svmParser;

    private String createCode() throws IOException, TypeException {
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
        final CodeGenerationASTVisitor codeGenerationASTVisitor = new CodeGenerationASTVisitor();
        return codeGenerationASTVisitor.visit(ast);
    }

    @BeforeEach
    void setUp() throws IOException, TypeException {
        this.code = createCode();
        final CharStream charsASM = CharStreams.fromString(code);
        final SVMLexer lexerASM = new SVMLexer(charsASM);
        final CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        this.svmParser = new SVMParser(tokensASM);
    }


    @Test
    void testAssemblyShouldHaveZeroLexicalErrors() {
        svmParser.assembly();
        assertEquals(0, svmParser.getNumberOfSyntaxErrors());
    }

    @Test
    void testAssemblyShouldHaveZeroSyntacticErrors() {
        svmParser.assembly();
        assertEquals(0, svmParser.getNumberOfSyntaxErrors());
    }

}
