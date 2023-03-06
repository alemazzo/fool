package compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class BasicLexerParserTest {

    private FOOLLexer lexer;
    private FOOLParser parser;


    @BeforeEach
    void setUp() throws Exception {
        String fileName = "examples/prova.fool";
        final CharStream chars = CharStreams.fromFileName(fileName);
        lexer = new FOOLLexer(chars);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser = new FOOLParser(tokens);
    }

    @Test
    void testWrongFileName() {
        final String wrongFileName = "examples/wrong.fool";
        assertThrows(IOException.class, () -> CharStreams.fromFileName(wrongFileName));
    }


    @Test
    void testZeroLexicalErrors() {
        parser.prog();
        assertEquals(0, lexer.lexicalErrors);
    }

    @Test
    void testZeroSyntaxErrors() {
        parser.prog();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
    }


}
