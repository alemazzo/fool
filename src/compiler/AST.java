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
        final List<DecNode> declarations;
        final Node exp;

        ProgLetInNode(final List<DecNode> declarations, final Node exp) {
            this.declarations = Collections.unmodifiableList(declarations);
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
        final TypeNode returnType;
        final List<ParNode> parameters;
        final List<DecNode> declarations;
        final Node exp;

        FunNode(final String id, final TypeNode returnType, final List<ParNode> parameters, final List<DecNode> declarations, final Node exp) {
            this.id = id;
            this.returnType = returnType;
            this.parameters = Collections.unmodifiableList(parameters);
            this.declarations = Collections.unmodifiableList(declarations);
            this.exp = exp;
        }

        void setType(final TypeNode type) {
            this.type = type;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ParNode extends DecNode {
        final String id;

        ParNode(final String id, final TypeNode type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class VarNode extends DecNode {
        final String id;
        final Node exp;

        VarNode(final String id, final TypeNode type, final Node exp) {
            this.id = id;
            this.type = type;
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class PrintNode extends Node {
        final Node exp;

        PrintNode(final Node exp) {
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IfNode extends Node {
        final Node condition;
        final Node thenBranch;
        final Node elseBranch;

        IfNode(final Node condition, final Node thenBranch, final Node elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class EqualNode extends Node {
        final Node left;
        final Node right;

        EqualNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IdNode extends Node {

        final String id;
        STentry entry;
        int nestingLevel;

        IdNode(final String id) {
            this.id = id;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class BoolNode extends Node {

        final Boolean value;

        BoolNode(final boolean value) {
            this.value = value;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class IntNode extends Node {

        final Integer value;

        IntNode(final Integer value) {
            this.value = value;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class ArrowTypeNode extends TypeNode {

        final List<TypeNode> parameters;
        final TypeNode returnType;

        ArrowTypeNode(final List<TypeNode> parameters, final TypeNode returnType) {
            this.parameters = Collections.unmodifiableList(parameters);
            this.returnType = returnType;
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
        final List<Node> arguments;
        STentry entry;
        int nestingLevel;

        CallNode(final String id, final List<Node> arguments) {
            this.id = id;
            this.arguments = Collections.unmodifiableList(arguments);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class GreaterEqualNode extends Node {
        final Node left;
        final Node right;

        GreaterEqualNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class LessEqualNode extends Node {
        final Node left;
        final Node right;

        LessEqualNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class NotNode extends Node {
        final Node exp;

        NotNode(final Node exp) {
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class OrNode extends Node {
        final Node left;
        final Node right;

        OrNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class AndNode extends Node {
        final Node left;
        final Node right;

        AndNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class TimesNode extends Node {
        final Node left;
        final Node right;

        TimesNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class DivNode extends Node {
        final Node left;
        final Node right;

        DivNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class PlusNode extends Node {
        final Node left;
        final Node right;

        PlusNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class MinusNode extends Node {
        final Node left;
        final Node right;

        MinusNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    public static class ClassNode extends DecNode {

        final String classId;
        final Optional<String> superId;
        final List<FieldNode> fields;
        final List<MethodNode> methods;
        ClassTypeNode type;
        STentry superEntry;

        public ClassNode(final String classId, final Optional<String> superId, final List<FieldNode> fields, final List<MethodNode> methods) {
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

        public FieldNode(final String fieldId, final TypeNode type) {
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

        String label;

        public MethodNode(final String methodId, final TypeNode returnType, final List<ParNode> params, final List<DecNode> declarations, final Node exp) {
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

        public ClassCallNode(final String objectId, final String methodId, final List<Node> args) {
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

        public NewNode(final String classId, final List<Node> args) {
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

        public ClassTypeNode(final List<TypeNode> fields, final List<ArrowTypeNode> methods) {
            this.fields = new ArrayList<>(fields);
            this.methods = new ArrayList<>(methods);
        }

        public ClassTypeNode(final ClassTypeNode parent) {
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

        public MethodTypeNode(final ArrowTypeNode functionalType) {
            this.functionalType = functionalType;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    public static class RefTypeNode extends TypeNode {

        final String typeId;

        public RefTypeNode(final String typeId) {
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














