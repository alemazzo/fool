package compiler;

import compiler.AST.ProgLetInNode;
import compiler.AST.ProgNode;
import compiler.exc.TypeException;
import compiler.lib.Node;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import svm.ExecuteVM;
import svm.SVMLexer;
import svm.SVMParser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.antlr.v4.runtime.CharStreams.fromString;

public class CodeUtils {

    private static final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private static final PrintStream ps = new PrintStream(outputStream);
    private static PrintStream old = System.out;

    public static ParseTree getParseTree(final CharStream chars) {
        FOOLLexer lexer = new FOOLLexer(chars);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);
        return parser.prog();
    }

    public static Node getAST(final CharStream chars) {
        final ParseTree st = getParseTree(chars);
        ASTGenerationSTVisitor visitor = new ASTGenerationSTVisitor(); // use true to visualize the ST
        return visitor.visit(st);
    }

    public static ProgNode getProgNodeFromAST(final CharStream chars) {
        return (ProgNode) getAST(chars);
    }

    public static ProgLetInNode getProgLetInNodeFromAST(final CharStream chars) {
        return (ProgLetInNode) getAST(chars);
    }

    public static Node getEAST(final CharStream chars) {
        final Node ast = getAST(chars);
        SymbolTableASTVisitor symtableVisitor = new SymbolTableASTVisitor(false);
        symtableVisitor.visit(ast);
        return ast;
    }

    public static ProgNode getProgNodeFromEAST(final CharStream chars) {
        return (ProgNode) getEAST(chars);
    }

    public static ProgLetInNode getProgLetInNodeFromEAST(final CharStream chars) {
        return (ProgLetInNode) getEAST(chars);
    }

    public static void checkTypes(final Node east) throws TypeException {
        TypeCheckEASTVisitor typeCheckVisitor = new TypeCheckEASTVisitor();
        typeCheckVisitor.visit(east);
    }

    public static String getAssembly(final CharStream chars) {
        final Node east = getEAST(chars);
        try {
            checkTypes(east);
        } catch (TypeException e) {
            throw new RuntimeException(e);
        }
        return new CodeGenerationASTVisitor().visit(east);
    }

    public static int[] getSVMCode(final CharStream chars) {
        final String code = getAssembly(chars);
        final SVMLexer lexerASM = new SVMLexer(fromString(code));
        final CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        final SVMParser parserASM = new SVMParser(tokensASM);
        parserASM.assembly();
        return parserASM.code;
    }

    public static void interceptOutput() {
        old = System.out;
        System.setOut(ps);
    }

    public static String getOutput() {
        System.setOut(ps);
        System.out.flush();
        System.setOut(old);
        final String output = outputStream.toString();
        return output.substring(0, output.length() - 1);
    }

    public static String getOutput(final CharStream chars) {
        final int[] code = getSVMCode(chars);
        final ExecuteVM vm = new ExecuteVM(code);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(outputStream);

        // IMPORTANT: Save the old System.out!
        final PrintStream old = System.out;

        // Tell Java to use your special stream
        System.setOut(ps);
        vm.cpu();
        // Put things back
        System.out.flush();
        System.setOut(old);

        // Remove the last newline
        final String output = outputStream.toString();
        return output.substring(0, output.length() - 1);
    }

}
