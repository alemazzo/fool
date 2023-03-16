package compiler;

import compiler.AST.*;
import compiler.exc.UnimplException;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

    CodeGenerationASTVisitor() {
        super(false);
    }

    CodeGenerationASTVisitor(boolean debug) {
        super(false, debug);
    }

    @Override
    public String visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        String declarationsCode = null;
        for (Node declaration : n.decList) {
            declarationsCode = nlJoin(declarationsCode, visit(declaration));
        }
        return nlJoin(
                "push 0",
                declarationsCode, // generate code for declarations (allocation)
                visit(n.exp),
                "halt",
                getCode()
        );
    }

    @Override
    public String visitNode(ProgNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.exp),
                "halt"
        );
    }

    @Override
    public String visitNode(FunNode n) {
        if (print) printNode(n, n.id);
        String declarationsCode = null, popDeclarationsCode = null, popParametersCode = null;
        for (Node declaration : n.declist) {
            declarationsCode = nlJoin(declarationsCode, visit(declaration));
            popDeclarationsCode = nlJoin(popDeclarationsCode, "pop");
        }
        for (int i = 0; i < n.parlist.size(); i++) {
            popParametersCode = nlJoin(popParametersCode, "pop");
        }
        final String funLabel = freshFunLabel();
        putCode(
                nlJoin(
                        funLabel + ":",
                        "cfp", // set $fp to $sp value
                        "lra", // load $ra value
                        declarationsCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(n.exp), // generate code for function body expression
                        "stm", // set $tm to popped value (function result)
                        popDeclarationsCode, // remove local declarations from stack
                        "sra", // set $ra to popped value
                        "pop", // remove Access Link from stack
                        popParametersCode, // remove parameters from stack
                        "sfp", // set $fp to popped value (Control Link)
                        "ltm", // load $tm value (function result)
                        "lra", // load $ra value
                        "js"  // jump to to popped address
                )
        );
        return "push " + funLabel;
    }

    @Override
    public String visitNode(VarNode n) {
        if (print) printNode(n, n.id);
        return visit(n.exp);
    }

    @Override
    public String visitNode(PrintNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.exp),
                "print"
        );
    }

    @Override
    public String visitNode(IfNode n) {
        if (print) printNode(n);
        String thenLabel = freshLabel();
        String endLabel = freshLabel();
        return nlJoin(
                visit(n.cond),
                "push 1",
                "beq " + thenLabel,
                visit(n.el),
                "b " + endLabel,
                thenLabel + ":",
                visit(n.th),
                endLabel + ":"
        );
    }

    @Override
    public String visitNode(EqualNode n) {
        if (print) printNode(n);
        String trueLabel = freshLabel();
        String endLabel = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "beq " + trueLabel,
                "push 0",
                "b " + endLabel,
                trueLabel + ":",
                "push 1",
                endLabel + ":"
        );
    }

    @Override
    public String visitNode(TimesNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "mult"
        );
    }

    @Override
    public String visitNode(PlusNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "add"
        );
    }

    @Override
    public String visitNode(CallNode n) {
        if (print) printNode(n, n.id);
        String argumentsCode = null, getARCode = null;
        for (int i = n.arglist.size() - 1; i >= 0; i--) {
            argumentsCode = nlJoin(argumentsCode, visit(n.arglist.get(i)));
        }
        for (int i = 0; i < n.nl - n.entry.nl; i++) {
            getARCode = nlJoin(getARCode, "lw");
        }
        return nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                argumentsCode, // generate code for argument expressions in reversed order
                "lfp", getARCode, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm", // duplicate top of stack
                "push " + n.entry.offset, "add", // compute address of "id" declaration
                "lw", // load address of "id" function
                "js"  // jump to popped address (saving address of subsequent instruction in $ra)
        );
    }

    @Override
    public String visitNode(IdNode n) {
        if (print) printNode(n, n.id);
        String getARCode = null;
        for (int i = 0; i < n.nl - n.entry.nl; i++) {
            getARCode = nlJoin(getARCode, "lw");
        }
        return nlJoin(
                "lfp", getARCode, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "push " + n.entry.offset, "add", // compute address of "id" declaration
                "lw" // load value of "id" variable
        );
    }

    @Override
    public String visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return "push " + (n.val ? 1 : 0);
    }

    @Override
    public String visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return "push " + n.val;
    }

    // ******************
    // ******************
    // OPERATOR EXTENSION
    // ******************
    // ******************

    @Override
    public String visitNode(MinusNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "sub"
        );
    }

    @Override
    public String visitNode(DivNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "div"
        );
    }

    @Override
    public String visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        final String falseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "push 1",
                "sub",
                "bleq " + falseLabel,   //if less , then false
                "push 1",
                "b " + endLabel,
                falseLabel + ":",       //false
                "push 0",
                endLabel + ":"          //end
        );
    }

    @Override
    public String visitNode(LessEqualNode n) {
        if (print) printNode(n);
        final String endLabel = freshLabel();
        final String trueLabel = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "bleq " + trueLabel,    //if less or equal, then true
                "push 0",
                "b " + endLabel,
                trueLabel + ":",        //true
                "push 1",
                endLabel + ":"          //end
        );
    }

    @Override
    public String visitNode(NotNode n) {
        if (print) printNode(n);
        final String itWasFalseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(n.exp),
                "push 0",
                "beq " + itWasFalseLabel,
                "push 0",
                "b " + endLabel,
                itWasFalseLabel + ":",
                "push 1",
                endLabel + ":"
        );
    }

    @Override
    public String visitNode(OrNode n) {
        if (print) printNode(n);
        final String trueLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(n.left),
                "push 1",
                "beq " + trueLabel,
                visit(n.right),
                "push 1",
                "beq " + trueLabel,
                "push 0",
                "b " + endLabel,
                trueLabel + ":",
                "push 1",
                endLabel + ":"
        );
    }

    @Override
    public String visitNode(AndNode n) {
        if (print) printNode(n);
        final String falseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(n.left),
                "push 0",
                "beq " + falseLabel,
                visit(n.right),
                "push 0",
                "beq " + falseLabel,
                "push 1",
                "b " + endLabel,
                falseLabel + ":",
                "push 0",
                endLabel + ":"
        );
    }

    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    @Override
    public String visitNode(ClassNode n) {
        if (print) printNode(n);
        throw new UnimplException();
    }

    @Override
    public String visitNode(FieldNode node) {
        if (print) printNode(node);
        throw new UnimplException();
    }

    @Override
    public String visitNode(MethodNode n) {
        if (print) printNode(n);
        throw new UnimplException();
    }

    @Override
    public String visitNode(ClassCallNode node) {
        if (print) printNode(node);
        throw new UnimplException();
    }

    @Override
    public String visitNode(NewNode n) {
        if (print) printNode(n);
        throw new UnimplException();
    }

    @Override
    public String visitNode(EmptyNode n) {
        if (print) printNode(n);
        throw new UnimplException();
    }

    @Override
    public String visitNode(ClassTypeNode n) {
        if (print) printNode(n);
        throw new UnimplException();
    }

    @Override
    public String visitNode(MethodTypeNode n) {
        if (print) printNode(n);
        throw new UnimplException();
    }

    @Override
    public String visitNode(RefTypeNode n) {
        if (print) printNode(n);
        throw new UnimplException();
    }

    @Override
    public String visitNode(EmptyTypeNode n) {
        if (print) printNode(n);
        throw new UnimplException();
    }

}