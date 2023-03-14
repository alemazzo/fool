package compiler;

import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.FOOLlib;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import svm.ExecuteVM;
import svm.SVMLexer;
import svm.SVMParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class CodeTester {

    private static String getOutputFromCharStream(final CharStream chars) {
        FOOLLexer lexer = new FOOLLexer(chars);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);

        System.out.println("Generating ST via lexer and parser.");
        ParseTree st = parser.prog();
        System.out.println("You had " + lexer.lexicalErrors + " lexical errors and " +
                parser.getNumberOfSyntaxErrors() + " syntax errors.\n");

        System.out.println("Generating AST.");
        ASTGenerationSTVisitor visitor = new ASTGenerationSTVisitor(); // use true to visualize the ST
        Node ast = visitor.visit(st);
        System.out.println();

        System.out.println("Enriching AST via symbol table.");
        SymbolTableASTVisitor symtableVisitor = new SymbolTableASTVisitor(true);
        symtableVisitor.visit(ast);
        System.out.println("You had " + symtableVisitor.stErrors + " symbol table errors.\n");

        System.out.println("Visualizing Enriched AST.");
        //new PrintEASTVisitor().visit(ast);
        System.out.println();

        System.out.println("Checking Types.");
        try {
            TypeCheckEASTVisitor typeCheckVisitor = new TypeCheckEASTVisitor();
            TypeNode mainType = typeCheckVisitor.visit(ast);
            System.out.print("Type of main program expression is: ");
            new PrintEASTVisitor().visit(mainType);
        } catch (IncomplException e) {
            System.out.println("Could not determine main program expression type due to errors detected before type checking.");
        } catch (TypeException e) {
            System.out.println("Type checking error in main program expression: " + e.text);
        }
        System.out.println("You had " + FOOLlib.typeErrors + " type checking errors.\n");

        int frontEndErrors = lexer.lexicalErrors + parser.getNumberOfSyntaxErrors() + symtableVisitor.stErrors + FOOLlib.typeErrors;
        System.out.println("You had a total of " + frontEndErrors + " front-end errors.\n");

        if (frontEndErrors > 0) System.exit(1);

        System.out.println("Generating code.");
        String code = new CodeGenerationASTVisitor().visit(ast);

        System.out.println("Assembling generated code.");
        CharStream charsASM = CharStreams.fromString(code);
        SVMLexer lexerASM = new SVMLexer(charsASM);
        CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        SVMParser parserASM = new SVMParser(tokensASM);

        parserASM.assembly();

        // needed only for debug
        System.out.println("You had: " + lexerASM.lexicalErrors + " lexical errors and " + parserASM.getNumberOfSyntaxErrors() + " syntax errors.\n");
        if (lexerASM.lexicalErrors + parserASM.getNumberOfSyntaxErrors() > 0) System.exit(1);

        System.out.println("Running generated code via Stack Virtual Machine.");
        ExecuteVM vm = new ExecuteVM(parserASM.code);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        // IMPORTANT: Save the old System.out!
        PrintStream old = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);
        vm.cpu();
        // Put things back
        System.out.flush();
        System.setOut(old);

        // Remove the last newline
        String output = baos.toString();
        return output.substring(0, output.length() - 1);
    }

    public static String getOutputFromSourceFile(final String path) {
        try {
            return getOutputFromCharStream(CharStreams.fromFileName(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getOutputFromSourceCode(final String sourceCode) {
        return getOutputFromCharStream(CharStreams.fromString(sourceCode));
    }

}
