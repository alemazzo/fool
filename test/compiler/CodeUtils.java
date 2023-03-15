package compiler;

import compiler.exc.TypeException;
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
import java.io.PrintStream;

public class CodeUtils {

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

    public static Node getEAST(final CharStream chars) {
        final Node ast = getAST(chars);
        SymbolTableASTVisitor symtableVisitor = new SymbolTableASTVisitor(true);
        symtableVisitor.visit(ast);
        return ast;
    }

    public static void checkTypes(final Node east) throws TypeException {
        TypeCheckEASTVisitor typeCheckVisitor = new TypeCheckEASTVisitor();
        TypeNode mainType = typeCheckVisitor.visit(east);
        new PrintEASTVisitor().visit(mainType);
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

        CharStream charsASM = CharStreams.fromString(code);
        SVMLexer lexerASM = new SVMLexer(charsASM);
        CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        SVMParser parserASM = new SVMParser(tokensASM);

        parserASM.assembly();
        return parserASM.code;
    }


    public static String getOutput(final CharStream chars) {

        final int[] code = getSVMCode(chars);

        ExecuteVM vm = new ExecuteVM(code);

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

}
