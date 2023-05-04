package compiler;

import compiler.lib.BaseASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AST {

    public static class ProgLetInNode extends Node {
        final List<DecNode> decList;
        final Node exp;

        ProgLetInNode(final List<DecNode> decList, final Node exp) {
            this.decList = Collections.unmodifiableList(decList);
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ProgNode extends Node {
        final Node exp;

        ProgNode(final Node exp) {
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class FunNode extends DecNode {
        final String id;
        final TypeNode retType;
        final List<ParNode> parlist;
        final List<DecNode> declist;
        final Node exp;

        FunNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
            id = i;
            retType = rt;
            parlist = Collections.unmodifiableList(pl);
            declist = Collections.unmodifiableList(dl);
            exp = e;
        }

        //void setType(TypeNode t) {type = t;}

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ParNode extends DecNode {
        final String id;

        ParNode(String i, TypeNode t) {
            id = i;
            type = t;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class VarNode extends DecNode {
        final String id;
        final Node exp;

        VarNode(String i, TypeNode t, Node v) {
            id = i;
            type = t;
            exp = v;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class PrintNode extends Node {
        final Node exp;

        PrintNode(Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IfNode extends Node {
        final Node cond;
        final Node th;
        final Node el;

        IfNode(Node c, Node t, Node e) {
            cond = c;
            th = t;
            el = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class EqualNode extends Node {
        final Node left;
        final Node right;

        EqualNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IdNode extends Node {

        final String id;
        STentry entry;
        int nl;

        IdNode(String i) {
            id = i;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class BoolNode extends Node {

        final Boolean val;

        BoolNode(boolean n) {
            val = n;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class IntNode extends Node {

        final Integer val;

        IntNode(Integer n) {
            val = n;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class ArrowTypeNode extends TypeNode {

        final List<TypeNode> parlist;
        final TypeNode ret;

        ArrowTypeNode(List<TypeNode> p, TypeNode r) {
            parlist = Collections.unmodifiableList(p);
            ret = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class BoolTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }


    }

    public static class IntTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class CallNode extends Node {
        final String id;
        final List<Node> arglist;
        STentry entry;
        int nl;

        CallNode(String i, List<Node> p) {
            id = i;
            arglist = Collections.unmodifiableList(p);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class GreaterEqualNode extends Node {
        final Node left;

        final Node right;

        GreaterEqualNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class LessEqualNode extends Node {
        final Node left;

        final Node right;

        LessEqualNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class NotNode extends Node {


        final Node exp;

        NotNode(Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class OrNode extends Node {
        final Node left;

        final Node right;

        OrNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class AndNode extends Node {
        final Node left;

        final Node right;

        AndNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class TimesNode extends Node {
        final Node left;

        final Node right;

        TimesNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class DivNode extends Node {

        final Node left;

        final Node right;

        DivNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class PlusNode extends Node {
        final Node left;

        final Node right;

        PlusNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class MinusNode extends Node {

        final Node left;

        final Node right;

        MinusNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ClassNode extends DecNode {

        final String classId;
        final Optional<String> superId;
        final List<FieldNode> fields;
        final List<MethodNode> methods;

        ClassTypeNode type;

        public ClassNode(String classId, Optional<String> superId, List<FieldNode> fields, List<MethodNode> methods) {
            this.classId = classId;
            this.superId = superId;
            this.fields = fields;
            this.methods = methods;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class FieldNode extends DecNode {

        final String fieldId;
        int offset;

        public FieldNode(String fieldId, TypeNode type) {
            this.fieldId = fieldId;
            this.type = type;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class MethodNode extends DecNode {

        final String methodId;
        final TypeNode returnType;
        final List<ParNode> params;
        final List<DecNode> declarations;
        final Node exp;

        int offset = 0;

        public MethodNode(String methodId, TypeNode returnType, List<ParNode> params, List<DecNode> declarations, Node exp) {
            this.methodId = methodId;
            this.returnType = returnType;
            this.params = params;
            this.declarations = declarations;
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class ClassCallNode extends Node {

        final String objectId;
        final String methodId;
        final List<Node> args;

        int nestingLevel = 0;
        STentry entry;
        STentry methodEntry;

        public ClassCallNode(String objectId, String methodId, List<Node> args) {
            this.objectId = objectId;
            this.methodId = methodId;
            this.args = args;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class NewNode extends Node {

        final String classId;
        final List<Node> args;
        STentry entry;

        public NewNode(String classId, List<Node> args) {
            this.classId = classId;
            this.args = args;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class EmptyNode extends Node {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class ClassTypeNode extends TypeNode {

        final List<TypeNode> fields;
        final List<ArrowTypeNode> methods;

        public ClassTypeNode(List<TypeNode> fields, List<ArrowTypeNode> methods) {
            this.fields = new ArrayList<>(fields);
            this.methods = new ArrayList<>(methods);
        }

        public ClassTypeNode(ClassTypeNode parent) {
            this(parent.fields, parent.methods);
        }

        public ClassTypeNode() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class MethodTypeNode extends TypeNode {

        final ArrowTypeNode functionalType;

        public MethodTypeNode(ArrowTypeNode functionalType) {
            this.functionalType = functionalType;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class RefTypeNode extends TypeNode {

        final String typeId;

        public RefTypeNode(String typeId) {
            this.typeId = typeId;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class EmptyTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

}














