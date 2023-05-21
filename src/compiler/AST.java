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

    /* *******************
     *********************
     * Main program nodes
     *********************
     ******************* */

    /**
     * The root node of the AST.
     * It contains a list of declarations and the main expression.
     */
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

    /**
     * The root node of the AST.
     * It contains the main expression.
     */
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

    /* *******************
     *********************
     * Basic Declaration Nodes
     *********************
     ******************* */

    /**
     * A function declaration node.
     * It contains the function name, the return type, the list of parameters,
     * the list of local declarations and the body expression.
     */
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

    /**
     * A parameter declaration node.
     * It contains the parameter id and type.
     */
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

    /**
     * A variable declaration node.
     * It contains the variable id, type and the initialization expression.
     */
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


    /* *******************
     *********************
     * Operators Nodes
     *********************
     ******************* */

    /**
     * The node for the if then else expression.
     * It contains the condition, the then branch and the else branch.
     */
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

    /**
     * The node for the not expression.
     * It contains the expression to negate.
     */
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

    /**
     * The node for the or expression.
     * It contains the left and right expression.
     */
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

    /**
     * The node for the and expression.
     * It contains the left and right expression.
     */
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

    /**
     * The node for the equal expression.
     * It contains the left and right expression.
     */
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

    /**
     * The node for the less equal expression.
     * It contains the left and right expression.
     */
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

    /**
     * The node for the greater equal expression.
     * It contains the left and right expression.
     */
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

    /**
     * The node for the times' expression.
     * It contains the left and right expression.
     */
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

    /**
     * The node for the div expression.
     * It contains the left and right expression.
     */
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

    /**
     * The node for the plus expression.
     * It contains the left and right expression.
     */
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

    /**
     * The node for the minus expression.
     * It contains the left and right expression.
     */
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

    /* *******************
     *********************
     * Values Nodes
     *********************
     ******************* */

    /**
     * The node for the bool value.
     * It contains the boolean value.
     */
    public static class BoolNode extends Node {

        final boolean value;

        BoolNode(final boolean value) {
            this.value = value;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /**
     * The node for the int value.
     * It contains the integer value.
     */
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

    /**
     * The node for the Id value, that is a variable.
     * It contains the id of the variable, the entry
     * in the symbol table and the nesting level.
     */
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

    /* *******************
     *********************
     * Types Nodes
     *********************
     ******************* */

    /**
     * The node for the arrow type, that is a function.
     * It contains the list of parameters and the return type.
     */
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

    /**
     * The node for the bool type.
     */
    public static class BoolTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }


    }

    /**
     * The node for the int type.
     */
    public static class IntTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /* *******************
     *********************
     * Operations Nodes
     *********************
     ******************* */

    /**
     * The node for the print operation.
     * It contains the expression to print.
     */
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

    /**
     * The node for the function call.
     * It contains the id of the function, the list of arguments,
     * the entry in the symbol table and the nesting level.
     */
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


    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    /* *******************
     *********************
     * Declaration Nodes
     *********************
     ******************* */

    /**
     * The node for the declaration of a class.
     * It contains the id of the class, the id of the super class,
     * the list of fields and the list of methods.
     * It also contains the type of the class and the entry of the super class.
     */
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
            this.fields = Collections.unmodifiableList(fields);
            this.methods = Collections.unmodifiableList(methods);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /**
     * The node for the field declaration.
     * It contains the id of the field and its type.
     * It also contains the offset of the field.
     */
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

    /**
     * The node for the method declaration.
     * It contains the id of the method, the return type,
     * the list of parameters, the list of local declarations
     * and the body of the method.
     * It also contains the offset of the method and the label
     * of the method used for the code generation.
     */
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
            this.params = Collections.unmodifiableList(params);
            this.declarations = Collections.unmodifiableList(declarations);
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /* *******************
     *********************
     * Value Nodes
     *********************
     ******************* */

    /**
     * The node for the null value.
     */
    public static class EmptyNode extends Node {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /* *******************
     *********************
     * Operations Nodes
     *********************
     ******************* */

    /**
     * The node for the method call.
     * It contains the id of the object, the id of the method,
     * the list of arguments and the entry in the symbol table.
     * It also contains the entry of the method in the symbol table.
     * It also contains the nesting level.
     */
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
            this.args = Collections.unmodifiableList(args);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /**
     * The node for the new expression.
     * It contains the id of the class and the list of arguments.
     * It also contains the entry in the symbol table.
     */
    public static class NewNode extends Node {

        final String classId;
        final List<Node> args;
        STentry entry;

        public NewNode(final String classId, final List<Node> args) {
            this.classId = classId;
            this.args = Collections.unmodifiableList(args);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

    /* *******************
     *********************
     * OOP Type Nodes
     *********************
     ******************* */

    /**
     * The node for the class type.
     * It contains the list of fields and the list of methods.
     */
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

    /**
     * The node for the method type.
     * It contains the functional type of the method.
     */
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

    /**
     * The node for the object reference type.
     * It contains the id of the class.
     */
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

    /**
     * The node for the null type.
     */
    public static class EmptyTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

    }

}














