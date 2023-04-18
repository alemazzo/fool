package compiler;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.exc.UnimplException;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static compiler.lib.FOOLlib.extractCtxName;
import static compiler.lib.FOOLlib.lowerizeFirstChar;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

    public boolean print;
    String indent;

    ASTGenerationSTVisitor() {
    }

    ASTGenerationSTVisitor(boolean debug) {
        print = debug;
    }

    private void printVarAndProdName(ParserRuleContext ctx) {
        String prefix = "";
        Class<?> ctxClass = ctx.getClass(), parentClass = ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
            prefix = lowerizeFirstChar(extractCtxName(parentClass.getName())) + ": production #";
        System.out.println(indent + prefix + lowerizeFirstChar(extractCtxName(ctxClass.getName())));
    }

    @Override
    public Node visit(ParseTree t) {
        if (t == null) return null;
        String temp = indent;
        indent = (indent == null) ? "" : indent + "  ";
        Node result = super.visit(t);
        indent = temp;
        return result;
    }

    @Override
    public Node visitProg(ProgContext c) {
        if (print) printVarAndProdName(c);
        return visit(c.progbody());
    }

    @Override
    public Node visitLetInProg(LetInProgContext c) {
        if (print) printVarAndProdName(c);
        List<DecNode> declist = new ArrayList<>();
        for (DecContext dec : c.dec()) declist.add((DecNode) visit(dec));
        return new ProgLetInNode(declist, visit(c.exp()));
    }

    @Override
    public Node visitNoDecProg(NoDecProgContext c) {
        if (print) printVarAndProdName(c);
        return new ProgNode(visit(c.exp()));
    }

    @Override
    public Node visitVardec(VardecContext c) {
        if (print) printVarAndProdName(c);
        Node n = null;
        if (c.ID() != null) { //non-incomplete ST
            n = new VarNode(c.ID().getText(), (TypeNode) visit(c.type()), visit(c.exp()));
            n.setLine(c.VAR().getSymbol().getLine());
        }
        return n;
    }

    @Override
    public Node visitFundec(FundecContext c) {
        if (print) printVarAndProdName(c);
        List<ParNode> parList = new ArrayList<>();
        for (int i = 1; i < c.ID().size(); i++) {
            ParNode p = new ParNode(c.ID(i).getText(), (TypeNode) visit(c.type(i)));
            p.setLine(c.ID(i).getSymbol().getLine());
            parList.add(p);
        }
        List<DecNode> decList = new ArrayList<>();
        for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));
        Node n = null;
        if (c.ID().size() > 0) { //non-incomplete ST
            n = new FunNode(c.ID(0).getText(), (TypeNode) visit(c.type(0)), parList, decList, visit(c.exp()));
            n.setLine(c.FUN().getSymbol().getLine());
        }
        return n;
    }

    @Override
    public Node visitIntType(IntTypeContext c) {
        if (print) printVarAndProdName(c);
        return new IntTypeNode();
    }

    @Override
    public Node visitBoolType(BoolTypeContext c) {
        if (print) printVarAndProdName(c);
        return new BoolTypeNode();
    }

    @Override
    public Node visitInteger(IntegerContext c) {
        if (print) printVarAndProdName(c);
        int v = Integer.parseInt(c.NUM().getText());
        return new IntNode(c.MINUS() == null ? v : -v);
    }

    @Override
    public Node visitTrue(TrueContext c) {
        if (print) printVarAndProdName(c);
        return new BoolNode(true);
    }

    @Override
    public Node visitFalse(FalseContext c) {
        if (print) printVarAndProdName(c);
        return new BoolNode(false);
    }

    @Override
    public Node visitIf(IfContext c) {
        if (print) printVarAndProdName(c);
        Node ifNode = visit(c.exp(0));
        Node thenNode = visit(c.exp(1));
        Node elseNode = visit(c.exp(2));
        Node n = new IfNode(ifNode, thenNode, elseNode);
        n.setLine(c.IF().getSymbol().getLine());
        return n;
    }

    @Override
    public Node visitPrint(PrintContext c) {
        if (print) printVarAndProdName(c);
        return new PrintNode(visit(c.exp()));
    }

    @Override
    public Node visitPars(ParsContext c) {
        if (print) printVarAndProdName(c);
        return visit(c.exp());
    }

    @Override
    public Node visitId(IdContext c) {
        if (print) printVarAndProdName(c);
        Node n = new IdNode(c.ID().getText());
        n.setLine(c.ID().getSymbol().getLine());
        return n;
    }

    @Override
    public Node visitCall(CallContext c) {
        if (print) printVarAndProdName(c);
        List<Node> arglist = new ArrayList<>();
        for (ExpContext arg : c.exp()) arglist.add(visit(arg));
        Node n = new CallNode(c.ID().getText(), arglist);
        n.setLine(c.ID().getSymbol().getLine());
        return n;
    }

    // ******************
    // ******************
    // OPERATOR EXTENSION
    // ******************
    // ******************

    @Override
    public Node visitTimesDiv(TimesDivContext c) {
        if (print) printVarAndProdName(c);
        if (c.TIMES() != null) {
            Node n = new TimesNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.TIMES().getSymbol().getLine());
            return n;
        } else {
            Node n = new DivNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.DIV().getSymbol().getLine());
            return n;
        }
    }

    @Override
    public Node visitPlusMinus(PlusMinusContext c) {
        if (print) printVarAndProdName(c);
        if (c.PLUS() != null) {
            Node n = new PlusNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.PLUS().getSymbol().getLine());
            return n;
        } else {
            Node n = new MinusNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.MINUS().getSymbol().getLine());
            return n;
        }
    }

    @Override
    public Node visitComp(CompContext c) {
        if (print) printVarAndProdName(c);
        if (c.EQ() != null) {
            Node n = new EqualNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.EQ().getSymbol().getLine());
            return n;
        } else if (c.GE() != null) {
            Node n = new GreaterEqualNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.GE().getSymbol().getLine());
            return n;
        } else {
            Node n = new LessEqualNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.LE().getSymbol().getLine());
            return n;
        }
    }

    @Override
    public Node visitAndOr(AndOrContext c) {
        if (print) printVarAndProdName(c);
        if (c.AND() != null) {
            Node n = new AndNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.AND().getSymbol().getLine());
            return n;
        } else {
            Node n = new OrNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.OR().getSymbol().getLine());
            return n;
        }
    }

    @Override
    public Node visitNot(NotContext c) {
        if (print) printVarAndProdName(c);
        Node n = new NotNode(visit(c.exp()));
        n.setLine(c.NOT().getSymbol().getLine());
        return n;
    }

    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    @Override
    public Node visitCldec(CldecContext c) {
        if (print) printVarAndProdName(c);
        final String classId = c.ID(0).getText();
        final Optional<String> superId = c.EXTENDS() == null ?
                Optional.empty() : Optional.of(c.ID(1).getText());
        final int idPadding = superId.isPresent() ? 2 : 1;
        final List<String> fieldIds = c.ID().stream()
                .skip(idPadding)
                .map(ParseTree::getText)
                .toList();
        final List<TypeNode> fieldTypes = c.type().stream()
                .map(this::visit)
                .map(n -> (TypeNode) n)
                .toList();
        final List<FieldNode> fields = IntStream.range(0, fieldIds.size())
                .mapToObj(i -> {
                    final FieldNode f = new FieldNode(fieldIds.get(i), fieldTypes.get(i));
                    f.setLine(c.ID(i + idPadding).getSymbol().getLine());
                    return f;
                })
                .toList();
        final List<MethodNode> methods = c.methdec().stream()
                .map(this::visit)
                .map(n -> (MethodNode) n)
                .toList();
        final ClassNode classNode = new ClassNode(classId, superId, fields, methods);
        classNode.setLine(c.ID(0).getSymbol().getLine());
        return classNode;
    }

    @Override
    public Node visitMethdec(MethdecContext c) {
        if (print) printVarAndProdName(c);
        final String methodId = c.ID(0).getText();
        final TypeNode returnType = (TypeNode) visit(c.type(0));
        final List<String> paramIds = c.ID().stream()
                .skip(1)
                .map(ParseTree::getText)
                .toList();
        final List<TypeNode> paramTypes = c.type().stream()
                .skip(1)
                .map(this::visit)
                .map(n -> (TypeNode) n)
                .toList();
        final List<ParNode> params = IntStream.range(0, paramIds.size())
                .mapToObj(i -> {
                    final ParNode p = new ParNode(paramIds.get(i), paramTypes.get(i));
                    p.setLine(c.ID(i + 1).getSymbol().getLine());
                    return p;
                })
                .toList();
        final List<DecNode> locals = c.dec().stream()
                .map(this::visit)
                .map(n -> (DecNode) n)
                .toList();
        final Node exp = visit(c.exp());
        final MethodNode methodNode = new MethodNode(methodId, returnType, params, locals, exp);
        methodNode.setLine(c.ID(0).getSymbol().getLine());
        return methodNode;
    }

    @Override
    public Node visitNew(NewContext c) {
        if (print) printVarAndProdName(c);
        final String classId = c.ID().getText();
        final List<Node> args = c.exp().stream()
                .map(this::visit)
                .toList();
        final NewNode newNode = new NewNode(classId, args);
        newNode.setLine(c.ID().getSymbol().getLine());
        return newNode;
    }

    @Override
    public Node visitDotCall(DotCallContext c) {
        if (print) printVarAndProdName(c);
        final String objectId = c.ID(0).getText();
        final String methodId = c.ID(1).getText();
        final List<Node> args = c.exp().stream()
                .map(this::visit)
                .toList();
        final ClassCallNode classCallNode = new ClassCallNode(objectId, methodId, args);
        classCallNode.setLine(c.ID(0).getSymbol().getLine());
        return classCallNode;
    }

    @Override
    public Node visitIdType(IdTypeContext c) {
        if (print) printVarAndProdName(c);
        throw new UnimplException();
    }

    @Override
    public Node visitNull(NullContext c) {
        if (print) printVarAndProdName(c);
        return new EmptyNode();
    }

}
