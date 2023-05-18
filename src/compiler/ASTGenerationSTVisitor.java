package compiler;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static compiler.lib.FOOLlib.extractCtxName;
import static compiler.lib.FOOLlib.lowerizeFirstChar;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

    public boolean print;
    String indent;

    public ASTGenerationSTVisitor() {
    }

    public ASTGenerationSTVisitor(boolean debug) {
        print = debug;
    }

    /**
     * Print the name of the current production and the name of the current variable context.
     *
     * @param context the current context
     */
    private void printVarAndProdName(final ParserRuleContext context) {
        String prefix = "";
        final Class<?> ctxClass = context.getClass();
        final Class<?> parentClass = ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) {
            // parentClass is the var context (and not ctxClass itself)
            prefix = lowerizeFirstChar(extractCtxName(parentClass.getName())) + ": production #";
        }
        System.out.println(indent + prefix + lowerizeFirstChar(extractCtxName(ctxClass.getName())));
    }

    /**
     * Increase the indentation level and visit the parse tree.
     *
     * @param parseTree the parse tree to visit
     * @return the result of the visit
     */
    @Override
    public Node visit(final ParseTree parseTree) {
        if (parseTree == null) return null;
        String temp = indent;
        indent = (indent == null) ? "" : indent + "  ";
        final Node result = super.visit(parseTree);
        indent = temp;
        return result;
    }

    /**
     * Visit the Prog context.
     * It visits the progbody and returns the result of the visit.
     *
     * @param context the Prog context
     * @return the result of the visit
     */
    @Override
    public Node visitProg(final ProgContext context) {
        if (print) printVarAndProdName(context);
        return visit(context.progbody());
    }

    /**
     * Visit the LetInProg context.
     * It visits the class declarations, the declarations and
     * the expression and returns the ProgLetInNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the ProgLetInNode built with the results of the visits
     */
    @Override
    public Node visitLetInProg(final LetInProgContext context) {
        if (print) printVarAndProdName(context);
        final List<DecNode> classDeclarations = context.cldec().stream()
                .map(x -> (DecNode) visit(x))
                .collect(Collectors.toList());
        final List<DecNode> declarations = context.dec().stream()
                .map(x -> (DecNode) visit(x))
                .collect(Collectors.toList());
        final List<DecNode> allDeclarations = new ArrayList<>();
        allDeclarations.addAll(classDeclarations);
        allDeclarations.addAll(declarations);
        return new ProgLetInNode(allDeclarations, visit(context.exp()));
    }

    /**
     * Visit the NoDecProg context.
     * It visits the expression and returns the ProgNode built with the result of the visit.
     *
     * @param context the parse tree
     * @return the ProgNode built with the result of the visit
     */
    @Override
    public Node visitNoDecProg(final NoDecProgContext context) {
        if (print) printVarAndProdName(context);
        return new ProgNode(visit(context.exp()));
    }

    /**
     * Visit the Vardec context.
     * It visits the type and the expression and returns the VarNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the VarNode built with the results of the visits
     */
    @Override
    public Node visitVardec(final VardecContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID() == null) return null; //incomplete ST
        final VarNode node = new VarNode(context.ID().getText(), (TypeNode) visit(context.type()), visit(context.exp()));
        node.setLine(context.VAR().getSymbol().getLine());
        return node;
    }

    /**
     * Visit the Fundec context.
     * It visits the type, the parameters, the declarations and
     * the expression and returns the FunNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the FunNode built with the results of the visits
     */
    @Override
    public Node visitFundec(final FundecContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID().size() == 0) return null; //incomplete ST

        List<ParNode> parametersList = new ArrayList<>();
        for (int i = 1; i < context.ID().size(); i++) {
            final ParNode p = new ParNode(context.ID(i).getText(), (TypeNode) visit(context.type(i)));
            p.setLine(context.ID(i).getSymbol().getLine());
            parametersList.add(p);
        }

        List<DecNode> declarationsList = new ArrayList<>();
        for (DecContext dec : context.dec()) declarationsList.add((DecNode) visit(dec));

        final String id = context.ID(0).getText();
        final TypeNode type = (TypeNode) visit(context.type(0));
        final FunNode node = new FunNode(id, type, parametersList, declarationsList, visit(context.exp()));
        node.setLine(context.FUN().getSymbol().getLine());
        return node;
    }

    /**
     * Visit the IntType context.
     * It returns the IntTypeNode.
     *
     * @param context the parse tree
     * @return the IntTypeNode
     */
    @Override
    public Node visitIntType(final IntTypeContext context) {
        if (print) printVarAndProdName(context);
        return new IntTypeNode();
    }

    /**
     * Visit the BoolType context.
     * It returns the BoolTypeNode.
     *
     * @param context the parse tree
     * @return the BoolTypeNode
     */
    @Override
    public Node visitBoolType(final BoolTypeContext context) {
        if (print) printVarAndProdName(context);
        return new BoolTypeNode();
    }

    /**
     * Visit the Integer context.
     * It returns the IntNode built with the value of the integer.
     * If the integer is negative, it returns the IntNode built with the opposite value.
     *
     * @param context the parse tree
     * @return the IntNode built with the value of the integer
     */
    @Override
    public Node visitInteger(final IntegerContext context) {
        if (print) printVarAndProdName(context);
        int value = Integer.parseInt(context.NUM().getText());
        int realValue = context.MINUS() == null ? value : -value;
        return new IntNode(realValue);
    }

    /**
     * Visit a True context.
     * It returns the BoolNode built with the value true.
     *
     * @param context the parse tree
     * @return the BoolNode built with the value true
     */
    @Override
    public Node visitTrue(final TrueContext context) {
        if (print) printVarAndProdName(context);
        return new BoolNode(true);
    }

    /**
     * Visit a False context.
     * It returns the BoolNode built with the value false.
     *
     * @param context the parse tree
     * @return the BoolNode built with the value false
     */
    @Override
    public Node visitFalse(final FalseContext context) {
        if (print) printVarAndProdName(context);
        return new BoolNode(false);
    }

    /**
     * Visit the If context.
     * It visits the three expressions and returns the IfNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the IfNode built with the results of the visits
     */
    @Override
    public Node visitIf(final IfContext context) {
        if (print) printVarAndProdName(context);
        final Node ifNode = visit(context.exp(0));
        final Node thenNode = visit(context.exp(1));
        final Node elseNode = visit(context.exp(2));
        final Node node = new IfNode(ifNode, thenNode, elseNode);
        node.setLine(context.IF().getSymbol().getLine());
        return node;
    }

    /**
     * Visit the Print context.
     * It visits the expression and returns the PrintNode built with the result of the visit.
     *
     * @param context the parse tree
     * @return the PrintNode built with the result of the visit
     */
    @Override
    public Node visitPrint(final PrintContext context) {
        if (print) printVarAndProdName(context);
        final Node exp = visit(context.exp());
        return new PrintNode(exp);
    }

    /**
     * Visit the Pars context.
     * It visits the expression and returns it as result of the visit.
     *
     * @param context the parse tree
     * @return the result of the visit of the expression
     */
    @Override
    public Node visitPars(final ParsContext context) {
        if (print) printVarAndProdName(context);
        return visit(context.exp());
    }

    /**
     * Visit the Id context.
     * It returns the IdNode built with the id.
     *
     * @param context the parse tree
     * @return the IdNode built with the id
     */
    @Override
    public Node visitId(final IdContext context) {
        if (print) printVarAndProdName(context);
        final String id = context.ID().getText();
        final Node node = new IdNode(id);
        node.setLine(context.ID().getSymbol().getLine());
        return node;
    }

    /**
     * Visit the Call context.
     * It returns the CallNode built with the id and the list of the arguments.
     *
     * @param context the parse tree
     * @return the CallNode built with the id and the list of the arguments
     */
    @Override
    public Node visitCall(final CallContext context) {
        if (print) printVarAndProdName(context);
        final String id = context.ID().getText();
        final List<Node> arglist = context.exp().stream()
                .map(this::visit)
                .collect(Collectors.toList());
        final Node node = new CallNode(id, arglist);
        node.setLine(context.ID().getSymbol().getLine());
        return node;
    }

    // ******************
    // ******************
    // OPERATOR EXTENSION
    // ******************
    // ******************

    /**
     * Visit the TimesDiv context.
     * It returns the TimesNode or the DivNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the TimesNode or the DivNode built with the results of the visits
     */
    @Override
    public Node visitTimesDiv(final TimesDivContext context) {
        if (print) printVarAndProdName(context);
        if (context.TIMES() != null) {
            // It's a TimesNode
            final Node node = new TimesNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.TIMES().getSymbol().getLine());
            return node;
        } else {
            // It's a DivNode
            final Node node = new DivNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.DIV().getSymbol().getLine());
            return node;
        }
    }

    /**
     * Visit the PlusMinus context.
     * It returns the PlusNode or the MinusNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the PlusNode or the MinusNode built with the results of the visits
     */
    @Override
    public Node visitPlusMinus(final PlusMinusContext context) {
        if (print) printVarAndProdName(context);
        if (context.PLUS() != null) {
            // It's a PlusNode
            final Node node = new PlusNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.PLUS().getSymbol().getLine());
            return node;
        } else {
            // It's a MinusNode
            final Node node = new MinusNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.MINUS().getSymbol().getLine());
            return node;
        }
    }

    /**
     * Visit the Comp context.
     * It returns the EqualNode, the GreaterEqualNode or the LessEqualNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the EqualNode, the GreaterEqualNode or the LessEqualNode built with the results of the visits
     */
    @Override
    public Node visitComp(final CompContext context) {
        if (print) printVarAndProdName(context);
        if (context.EQ() != null) {
            // It's an EqualNode
            final Node node = new EqualNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.EQ().getSymbol().getLine());
            return node;
        } else if (context.GE() != null) {
            // It's a GreaterEqualNode
            final Node node = new GreaterEqualNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.GE().getSymbol().getLine());
            return node;
        } else {
            // It's a LessEqualNode
            final Node node = new LessEqualNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.LE().getSymbol().getLine());
            return node;
        }
    }

    /**
     * Visit the AndOr context.
     * It returns the AndNode or the OrNode built with the results of the visits.
     *
     * @param context the parse tree
     * @return the AndNode or the OrNode built with the results of the visits
     */
    @Override
    public Node visitAndOr(final AndOrContext context) {
        if (print) printVarAndProdName(context);
        if (context.AND() != null) {
            // It's an AndNode
            final Node node = new AndNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.AND().getSymbol().getLine());
            return node;
        } else {
            // It's an OrNode
            final Node node = new OrNode(visit(context.exp(0)), visit(context.exp(1)));
            node.setLine(context.OR().getSymbol().getLine());
            return node;
        }
    }

    /**
     * Visit the Not context.
     * It returns the NotNode built with the result of the visit.
     *
     * @param context the parse tree
     * @return the NotNode built with the result of the visit
     */
    @Override
    public Node visitNot(final NotContext context) {
        if (print) printVarAndProdName(context);
        final Node node = new NotNode(visit(context.exp()));
        node.setLine(context.NOT().getSymbol().getLine());
        return node;
    }

    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    /**
     * Visit the Cldec context.
     * It returns the ClassNode built with the class id, the super id, the fields and the methods.
     *
     * @param context the parse tree
     * @return the ClassNode built with the class id, the super id, the fields and the methods
     */
    @Override
    public Node visitCldec(final CldecContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID().size() == 0) return null; // Incomplete ST
        final String classId = context.ID(0).getText();
        final Optional<String> superId = context.EXTENDS() == null ?
                Optional.empty() : Optional.of(context.ID(1).getText());
        final int idPadding = superId.isPresent() ? 2 : 1;
        final List<String> fieldIds = context.ID().stream()
                .skip(idPadding)
                .map(ParseTree::getText)
                .toList();
        final List<TypeNode> fieldTypes = context.type().stream()
                .map(x -> (TypeNode) visit(x))
                .toList();
        final List<FieldNode> fields = IntStream.range(0, fieldIds.size())
                .mapToObj(i -> {
                    final FieldNode f = new FieldNode(fieldIds.get(i), fieldTypes.get(i));
                    f.setLine(context.ID(i + idPadding).getSymbol().getLine());
                    return f;
                })
                .toList();
        final List<MethodNode> methods = context.methdec().stream()
                .map(x -> (MethodNode) visit(x))
                .toList();
        final ClassNode classNode = new ClassNode(classId, superId, fields, methods);
        classNode.setLine(context.ID(0).getSymbol().getLine());
        return classNode;
    }

    /**
     * Visit the Methdec context.
     * It returns the MethodNode built with the method id, the return type, the parameters, the declarations and the body.
     *
     * @param context the parse tree
     * @return the MethodNode built with the method id, the return type, the parameters, the declarations and the body
     */
    @Override
    public Node visitMethdec(final MethdecContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID().size() == 0) return null; // Incomplete ST
        final String methodId = context.ID(0).getText();
        final TypeNode returnType = (TypeNode) visit(context.type(0));
        final List<ParNode> params = IntStream.range(1, context.ID().size())
                .mapToObj(i -> {
                    final ParNode p = new ParNode(context.ID(i).getText(), (TypeNode) visit(context.type(i)));
                    p.setLine(context.ID(i).getSymbol().getLine());
                    return p;
                })
                .toList();
        final List<DecNode> declarations = context.dec().stream()
                .map(x -> (DecNode) visit(x))
                .toList();
        final Node exp = visit(context.exp());
        final MethodNode methodNode = new MethodNode(methodId, returnType, params, declarations, exp);
        methodNode.setLine(context.ID(0).getSymbol().getLine());
        return methodNode;
    }

    /**
     * Visit the New context.
     * It returns the NewNode built with the class id and the arguments.
     *
     * @param context the parse tree
     * @return the NewNode built with the class id and the arguments
     */
    @Override
    public Node visitNew(final NewContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID() == null) return null; // Incomplete ST
        final String classId = context.ID().getText();
        final List<Node> args = context.exp().stream()
                .map(this::visit)
                .toList();
        final NewNode newNode = new NewNode(classId, args);
        newNode.setLine(context.ID().getSymbol().getLine());
        return newNode;
    }

    /**
     * Visit the DotCall context.
     * It returns the ClassCallNode built with the object id, the method id and the arguments.
     *
     * @param context the parse tree
     * @return the ClassCallNode built with the object id, the method id and the arguments
     */
    @Override
    public Node visitDotCall(final DotCallContext context) {
        if (print) printVarAndProdName(context);
        if (context.ID().size() != 2) return null; // Incomplete ST
        final String objectId = context.ID(0).getText();
        final String methodId = context.ID(1).getText();
        final List<Node> args = context.exp().stream()
                .map(this::visit)
                .toList();
        final ClassCallNode classCallNode = new ClassCallNode(objectId, methodId, args);
        classCallNode.setLine(context.ID(0).getSymbol().getLine());
        return classCallNode;
    }

    /**
     * Visit the IdType context.
     * It returns the RefTypeNode built with the id.
     *
     * @param context the parse tree
     * @return the RefTypeNode built with the id
     */
    @Override
    public Node visitIdType(final IdTypeContext context) {
        if (print) printVarAndProdName(context);
        final String id = context.ID().getText();
        final RefTypeNode node = new RefTypeNode(id);
        node.setLine(context.ID().getSymbol().getLine());
        return node;
    }

    /**
     * Visit the Null context.
     * It returns the EmptyNode.
     *
     * @param context the parse tree
     * @return the EmptyNode
     */
    @Override
    public Node visitNull(final NullContext context) {
        if (print) printVarAndProdName(context);
        return new EmptyNode();
    }

}
