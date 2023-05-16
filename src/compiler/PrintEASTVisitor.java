package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.Node;

public class PrintEASTVisitor extends BaseEASTVisitor<Void, VoidException> {

    PrintEASTVisitor() {
        super(false, true);
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        printNode(n);
        for (Node dec : n.declarations) visit(dec);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        printNode(n, n.id);
        visit(n.returnType);
        for (ParNode par : n.parameters) visit(par);
        for (Node dec : n.declarations) visit(dec);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(ParNode n) {
        printNode(n, n.id);
        visit(n.getType());
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        printNode(n, n.id);
        visit(n.getType());
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        printNode(n);
        visit(n.condition);
        visit(n.thenBranch);
        visit(n.elseBranch);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode n) throws VoidException {
        printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) throws VoidException {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(MinusNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        printNode(n, n.id + " at nestinglevel " + n.nestingLevel);
        visit(n.entry);
        for (Node arg : n.arguments) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        printNode(n, n.id + " at nestinglevel " + n.nestingLevel);
        visit(n.entry);
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        printNode(n, n.value.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        printNode(n, n.value.toString());
        return null;
    }

    @Override
    public Void visitNode(ArrowTypeNode n) {
        printNode(n);
        for (Node par : n.parameters) visit(par);
        visit(n.returnType, "->"); //marks return type
        return null;
    }

    @Override
    public Void visitNode(BoolTypeNode n) {
        printNode(n);
        return null;
    }

    @Override
    public Void visitNode(IntTypeNode n) {
        printNode(n);
        return null;
    }

    @Override
    public Void visitSTentry(STentry entry) {
        printSTentry("nestlev " + entry.nl);
        printSTentry("type");
        visit(entry.type);
        printSTentry("offset " + entry.offset);
        return null;
    }


    // OBJECT-ORIENTED EXTENSION


    @Override
    public Void visitNode(ClassNode n) throws VoidException {
        var superClass = n.superId.isPresent() ? " extends: " + n.superId : "";
        printNode(n, n.classId + superClass);
        for (FieldNode par : n.fields) visit(par);
        for (Node dec : n.methods) visit(dec);
        return null;
    }

    @Override
    public Void visitNode(FieldNode n) throws VoidException {
        printNode(n, n.fieldId);
        visit(n.getType());
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) throws VoidException {
        printNode(n, n.methodId);
        visit(n.returnType);
        for (ParNode par : n.params) visit(par);
        for (Node dec : n.declarations) visit(dec);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) throws VoidException {
        printNode(n, n.objectId + "." + n.methodId + " at nestinglevel " + n.nestingLevel);
        visit(n.entry);
        visit(n.methodEntry);
        for (Node arg : n.args) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(NewNode n) throws VoidException {
        printNode(n, n.classId + " at nestinglevel " + n.entry.nl);
        visit(n.entry);
        for (Node arg : n.args) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) throws VoidException {
        printNode(n);
        return null;
    }

    @Override
    public Void visitNode(ClassTypeNode n) throws VoidException {
        printNode(n);
        for (var field : n.fields) visit(field);
        for (var method : n.methods) visit(method);
        return null;
    }

    @Override
    public Void visitNode(MethodTypeNode n) throws VoidException {
        printNode(n);
        for (Node par : n.functionalType.parameters) visit(par);
        visit(n.functionalType.returnType, "->"); //marks return type
        return null;
    }

    @Override
    public Void visitNode(RefTypeNode n) throws VoidException {
        printNode(n, n.typeId);
        return null;
    }
}