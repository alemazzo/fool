package compiler;

import compiler.AST.*;
import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode, TypeException> {

    public TypeCheckEASTVisitor(boolean incompleteExc, boolean debug) {
        super(incompleteExc, debug);
    }

    public TypeCheckEASTVisitor(boolean debug) {
        this(true, debug);
    }

    public TypeCheckEASTVisitor() {
        this(true, false);
    }


    /**
     * Checks that a type object is visitable (not incomplete).
     * If not, throws an exception.
     * Returns the type object.
     *
     * @param t the type object to check
     * @return the type object
     * @throws TypeException if the type object is not visitable
     */
    private TypeNode ckvisit(final TypeNode t) throws TypeException {
        visit(t);
        return t;
    }

    /**
     * Checks that a STentry object is visitable (not incomplete).
     * If not, throws an exception.
     * Returns the type object contained in the STentry object.
     *
     * @param entry the STentry object to check
     * @return the type object contained in the STentry object
     * @throws TypeException if the STentry object is not visitable
     */
    @Override
    public TypeNode visitSTentry(final STentry entry) throws TypeException {
        if (print) printSTentry("type");
        return ckvisit(entry.type);
    }

    /**
     * Visit a ProgLetInNode node and check its type.
     * For each declaration, visit it.
     * Then visit the expression and return its type.
     *
     * @param node the ProgLetInNode node to visit
     * @return the type of the expression
     * @throws TypeException if a declaration is not correct
     */
    @Override
    public TypeNode visitNode(final ProgLetInNode node) throws TypeException {
        if (print) printNode(node);
        for (final DecNode declaration : node.declarations) {
            try {
                visit(declaration);
            } catch (IncomplException ignored) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        }
        return visit(node.exp);
    }

    /**
     * Visit a ProgNode node and check its type.
     * Visit the expression and return its type.
     *
     * @param node the ProgNode node to visit
     * @return the type of the expression
     * @throws TypeException if the expression is not correct
     */
    @Override
    public TypeNode visitNode(final ProgNode node) throws TypeException {
        if (print) printNode(node);
        return visit(node.exp);
    }

    /**
     * Visit a FunNode node and check its type.
     * For each declaration, visit it.
     * Then visit the expression and check that its type is a subtype of the return type.
     * If not, throws an exception.
     *
     * @param node the FunNode node to visit
     * @return null
     * @throws TypeException if a declaration is not correct or the return type is not correct
     */
    @Override
    public TypeNode visitNode(final FunNode node) throws TypeException {
        if (print) printNode(node, node.id);
        for (Node dec : node.declarations) {
            try {
                visit(dec);
            } catch (IncomplException ignored) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        }
        if (!isSubtype(visit(node.exp), ckvisit(node.returnType))) {
            throw new TypeException("Wrong return type for function " + node.id, node.getLine());
        }
        return null;
    }

    /**
     * Visit a VarNode node and check its type.
     * Visit the expression and check that its type is a subtype of the variable type.
     * If not, throws an exception.
     *
     * @param node the VarNode node to visit
     * @return null
     * @throws TypeException if the expression is not correct
     */
    @Override
    public TypeNode visitNode(final VarNode node) throws TypeException {
        if (print) printNode(node, node.id);
        if (!isSubtype(visit(node.exp), ckvisit(node.getType()))) {
            throw new TypeException("Incompatible value for variable " + node.id, node.getLine());
        }
        return null;
    }

    /**
     * Visit a PrintNode node and check its type.
     * Simply visit the expression and return its type.
     *
     * @param node the PrintNode node to visit
     * @return the type of the expression
     * @throws TypeException if the expression is not correct
     */
    @Override
    public TypeNode visitNode(final PrintNode node) throws TypeException {
        if (print) printNode(node);
        return visit(node.exp);
    }

    /**
     * Visit a IfNode node and check its type.
     * Visit the condition and check that its type is boolean.
     * Then visit the then branch and the else branch.
     * Return the lowest common ancestor of the two types.
     * If not, throws an exception.
     *
     * @param node the IfNode node to visit
     * @return the lowest common ancestor of the types of the two branches
     * @throws TypeException if the condition is not boolean or the types of the branches are not compatible
     */
    @Override
    public TypeNode visitNode(final IfNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.condition), new BoolTypeNode()))) {
            throw new TypeException("Non boolean condition in if", node.getLine());
        }

        final TypeNode thenBranch = visit(node.thenBranch);
        final TypeNode elseBranch = visit(node.elseBranch);

        final TypeNode returnType = lowestCommonAncestor(thenBranch, elseBranch);
        if (returnType == null) {
            throw new TypeException("Incompatible types in then-else branches", node.getLine());
        }

        return returnType;
    }

    /**
     * Visit a EqualNode node and check its type.
     * Visit the left and right expressions and check that their types are compatible.
     * Return boolean.
     *
     * @param node the EqualNode node to visit
     * @return boolean
     * @throws TypeException if the types of the expressions are not compatible
     */
    @Override
    public TypeNode visitNode(final EqualNode node) throws TypeException {
        if (print) printNode(node);
        final TypeNode left = visit(node.left);
        final TypeNode right = visit(node.right);
        if (!(isSubtype(left, right) || isSubtype(right, left))) {
            throw new TypeException("Incompatible types in equal", node.getLine());
        }
        return new BoolTypeNode();
    }

    /**
     * Visit a TimesNode node and check its type.
     * Visit the left and right expressions and check that their types are integers.
     * Return integer.
     *
     * @param node the TimesNode node to visit
     * @return integer
     * @throws TypeException if the types of the expressions are not integers
     */
    @Override
    public TypeNode visitNode(final TimesNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in multiplication", node.getLine());
        }
        return new IntTypeNode();
    }

    /**
     * Visit a PlusNode node and check its type.
     * Visit the left and right expressions and check that their types are integers.
     * Return integer.
     *
     * @param node the PlusNode node to visit
     * @return integer
     * @throws TypeException if the types of the expressions are not integers
     */
    @Override
    public TypeNode visitNode(final PlusNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in sum", node.getLine());
        }
        return new IntTypeNode();
    }

    /**
     * Visit a CallNode node and check its type.
     * Visit the entry of the function and check that it is a function.
     * Then visit the arguments and check that their types are compatible with the types of the parameters.
     * Return the return type of the function.
     *
     * @param node the CallNode node to visit
     * @return the return type of the function
     * @throws TypeException if the entry is not a function or the types of the arguments are not compatible with the types of the parameters
     */
    @Override
    public TypeNode visitNode(final CallNode node) throws TypeException {
        if (print) printNode(node, node.id);
        TypeNode typeNode = visit(node.entry);

        if (typeNode instanceof MethodTypeNode methodTypeNode) {
            typeNode = methodTypeNode.functionalType;
        }

        if (!(typeNode instanceof ArrowTypeNode arrowTypeNode)) {
            throw new TypeException("Invocation of a non-function " + node.id, node.getLine());
        }

        if (!(arrowTypeNode.parameters.size() == node.arguments.size())) {
            throw new TypeException("Wrong number of parameters in the invocation of " + node.id, node.getLine());
        }
        
        for (int i = 0; i < node.arguments.size(); i++) {
            if (!(isSubtype(visit(node.arguments.get(i)), arrowTypeNode.parameters.get(i)))) {
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + node.id, node.getLine());
            }
        }
        return arrowTypeNode.returnType;
    }

    /**
     * Visit a IdNode node and check its type.
     * Visit the entry of the identifier and return its type.
     * If the type is a function, throws an exception.
     *
     * @param node the IdNode node to visit
     * @return the type of the identifier
     * @throws TypeException if the entry is a function
     */
    @Override
    public TypeNode visitNode(final IdNode node) throws TypeException {
        if (print) printNode(node, node.id);
        final TypeNode typeNode = visit(node.entry);
        if (typeNode instanceof ArrowTypeNode) {
            throw new TypeException("Wrong usage of function identifier " + node.id, node.getLine());
        }
        return typeNode;
    }

    /**
     * Visit a BoolNode node and check its type.
     * Return boolean.
     *
     * @param node the BoolNode node to visit
     * @return boolean
     */
    @Override
    public TypeNode visitNode(final BoolNode node) {
        if (print) printNode(node, node.value.toString());
        return new BoolTypeNode();
    }

    /**
     * Visit a IntNode node and check its type.
     * Return integer.
     *
     * @param node the IntNode node to visit
     * @return integer
     */
    @Override
    public TypeNode visitNode(final IntNode node) {
        if (print) printNode(node, node.value.toString());
        return new IntTypeNode();
    }

    /**
     * Visit a BoolTypeNode node.
     * Return null.
     *
     * @param node the BoolTypeNode node to visit
     * @return null
     */
    @Override
    public TypeNode visitNode(final BoolTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a IntTypeNode node.
     * Return null.
     *
     * @param node the IntTypeNode node to visit
     * @return null
     */
    @Override
    public TypeNode visitNode(final IntTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a ArrowTypeNode node.
     * Visit the parameters and the return type.
     * Return null.
     *
     * @param node the ArrowTypeNode node to visit
     * @return null
     */
    @Override
    public TypeNode visitNode(final ArrowTypeNode node) throws TypeException {
        if (print) printNode(node);
        for (final TypeNode parameter : node.parameters) {
            visit(parameter);
        }
        visit(node.returnType, "->"); //marks return type
        return null;
    }

    // ******************
    // ******************
    // OPERATOR EXTENSION
    // ******************
    // ******************

    /**
     * Visit a MinusNode node and check its type.
     * Visit the left and right expressions and check that their types are integers.
     * Return integer.
     *
     * @param node the MinusNode node to visit
     * @return integer
     * @throws TypeException if the types of the expressions are not integers
     */
    @Override
    public TypeNode visitNode(final MinusNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in minus", node.getLine());
        }
        return new IntTypeNode();
    }

    /**
     * Visit a DivNode node and check its type.
     * Visit the left and right expressions and check that their types are integers.
     * Return integer.
     *
     * @param node the DivNode node to visit
     * @return integer
     * @throws TypeException if the types of the expressions are not integers
     */
    @Override
    public TypeNode visitNode(final DivNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in Div", node.getLine());
        }
        return new IntTypeNode();
    }

    /**
     * Visit a GreaterEqualNode node and check its type.
     * Visit the left and right expressions and check that their types are integers.
     * Return boolean.
     *
     * @param node the GreaterEqualNode node to visit
     * @return boolean
     * @throws TypeException if the types of the expressions are not integers
     */
    @Override
    public TypeNode visitNode(final GreaterEqualNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in Gte", node.getLine());
        }
        return new BoolTypeNode();
    }

    /**
     * Visit a LessEqualNode node and check its type.
     * Visit the left and right expressions and check that their types are integers.
     * Return boolean.
     *
     * @param node the LessEqualNode node to visit
     * @return boolean
     * @throws TypeException if the types of the expressions are not integers
     */
    @Override
    public TypeNode visitNode(final LessEqualNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new IntTypeNode())
                && isSubtype(visit(node.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in Lte", node.getLine());
        }
        return new BoolTypeNode();
    }

    /**
     * Visit a NotNode node and check its type.
     * Visit the expression and check that its type is boolean.
     * Return boolean.
     *
     * @param node the NotNode node to visit
     * @return boolean
     * @throws TypeException if the type of the expression is not boolean
     */
    @Override
    public TypeNode visitNode(final NotNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.exp), new BoolTypeNode()))) {
            throw new TypeException("Non boolean in not", node.getLine());
        }
        return new BoolTypeNode();
    }

    /**
     * Visit a OrNode node and check its type.
     * Visit the left and right expressions and check that their types are boolean.
     * Return boolean.
     *
     * @param node the OrNode node to visit
     * @return boolean
     * @throws TypeException if the types of the expressions are not boolean
     */
    @Override
    public TypeNode visitNode(final OrNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new BoolTypeNode())
                && isSubtype(visit(node.right), new BoolTypeNode()))) {
            throw new TypeException("Non booleans in or", node.getLine());
        }
        return new BoolTypeNode();
    }

    /**
     * Visit a AndNode node and check its type.
     * Visit the left and right expressions and check that their types are boolean.
     * Return boolean.
     *
     * @param node the AndNode node to visit
     * @return boolean
     * @throws TypeException if the types of the expressions are not boolean
     */
    @Override
    public TypeNode visitNode(final AndNode node) throws TypeException {
        if (print) printNode(node);
        if (!(isSubtype(visit(node.left), new BoolTypeNode())
                && isSubtype(visit(node.right), new BoolTypeNode()))) {
            throw new TypeException("Non booleans in and", node.getLine());
        }
        return new BoolTypeNode();
    }


    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    /**
     * Visit a ClassNode node and check its type.
     * If the class has a super class, add it as super type in TypeRels Map.
     * Visit all methods in the class.
     * <p>
     * If the class has a super class, visit all fields and methods
     * in the class and check that their types are subtypes of the
     * types of the fields and methods in the super class.
     * Return null.
     *
     * @param node the ClassNode node to visit
     * @return null
     * @throws TypeException if the class has a super class and the super class is not defined
     */
    @Override
    public TypeNode visitNode(final ClassNode node) throws TypeException {
        if (print) printNode(node, node.classId);
        final boolean isSubClass = node.superId.isPresent();
        final String superId = isSubClass ? node.superId.get() : null;

        // if class has a super class, add it as super type in TypeRels Map
        if (isSubClass) {
            superType.put(node.classId, superId);
        }

        // visit all methods
        for (final MethodNode method : node.methods) {
            try {
                visit(method);
            } catch (TypeException e) {
                System.out.println("Type checking error in a class declaration: " + e.text);
            }
        }

        if (!isSubClass || node.superEntry == null) {
            return null;
        }

        final ClassTypeNode classType = node.type;
        final ClassTypeNode parentClassType = (ClassTypeNode) node.superEntry.type;

        // check if all fields and methods of the class are the correct subtypes and with the correct position
        for (final FieldNode field : node.fields) {
            int position = -field.offset - 1;
            if (position < parentClassType.fields.size()
                    && !isSubtype(classType.fields.get(position), parentClassType.fields.get(position))) {
                throw new TypeException("Wrong type for field " + field.fieldId, field.getLine());
            }
        }

        for (final MethodNode method : node.methods) {
            int position = method.offset;
            if (position < parentClassType.methods.size()
                    && !isSubtype(classType.methods.get(position), parentClassType.methods.get(position))) {
                throw new TypeException("Wrong type for method " + method.methodId, method.getLine());
            }
        }

        return null;
    }

    /**
     * Visit a MethodNode node and check its type.
     * Visit all declarations.
     * Visit the expression and check that its type is a subtype of the return type.
     *
     * @param node the MethodNode node to visit
     * @return null
     * @throws TypeException if the type of the expression is not a subtype of the return type
     */
    @Override
    public TypeNode visitNode(final MethodNode node) throws TypeException {
        if (print) printNode(node, node.methodId);

        for (final DecNode dec : node.declarations) {
            try {
                visit(dec);
            } catch (TypeException e) {
                System.out.println("Type checking error in a method declaration: " + e.text);
            }
        }
        // visit expression and check if it is a subtype of the return type
        if (!isSubtype(visit(node.exp), ckvisit(node.returnType))) {
            throw new TypeException("Wrong return type for method " + node.methodId, node.getLine());
        }

        return null;
    }

    /**
     * Visit a ClassCallNode node and check its type.
     * Visit the method entry and check that its type is a method type.
     * Check that the number of parameters is correct and that their types are correct.
     * Return the type of the method.
     *
     * @param node the ClassCallNode node to visit
     * @return the type of the method
     * @throws TypeException if the type of the method is not a method type
     *                       or if the number of parameters is not correct
     *                       or if the types of the parameters are not correct
     */
    @Override
    public TypeNode visitNode(final ClassCallNode node) throws TypeException {
        if (print) printNode(node, node.objectId);

        TypeNode type = visit(node.methodEntry);

        // visit method, if it is a method type, get the functional type
        if (type instanceof MethodTypeNode methodTypeNode) {
            type = methodTypeNode.functionalType;
        }

        // if it is not an arrow type, throw an exception
        if (!(type instanceof ArrowTypeNode arrowTypeNode)) {
            throw new TypeException("Invocation of a non-function " + node.methodId, node.getLine());
        }

        // check if the number of parameters is correct
        if (arrowTypeNode.parameters.size() != node.args.size()) {
            throw new TypeException("Wrong number of parameters in the invocation of method " + node.methodId, node.getLine());
        }

        // check if the types of the parameters are correct
        for (int i = 0; i < node.args.size(); i++) {
            if (!(isSubtype(visit(node.args.get(i)), arrowTypeNode.parameters.get(i)))) {
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of method " + node.methodId, node.getLine());
            }
        }

        return arrowTypeNode.returnType;
    }

    /**
     * Visit a NewNode node and check its type.
     * Visit the class entry and check that it is a class type.
     * Check that the number of parameters is correct and that their types are correct.
     *
     * @param node the NewNode node to visit
     * @return the class type
     * @throws TypeException if the class entry is not a class type or if the number of parameters is wrong or if their types are wrong
     */
    @Override
    public TypeNode visitNode(final NewNode node) throws TypeException {
        if (print) printNode(node, node.classId);
        final TypeNode typeNode = visit(node.entry);

        if (!(typeNode instanceof ClassTypeNode classTypeNode)) {
            throw new TypeException("Invocation of a non-constructor " + node.classId, node.getLine());
        }

        if (classTypeNode.fields.size() != node.args.size()) {
            throw new TypeException("Wrong number of parameters in the invocation of constructor " + node.classId, node.getLine());
        }
        // check if the types of the parameters are correct
        for (int i = 0; i < node.args.size(); i++) {
            if (!(isSubtype(visit(node.args.get(i)), classTypeNode.fields.get(i)))) {
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of constructor " + node.classId, node.getLine());
            }
        }
        return new RefTypeNode(node.classId);
    }


    /**
     * Visit an EmptyNode node and return an EmptyTypeNode.
     *
     * @param node the EmptyNode node to visit.
     * @return an EmptyTypeNode.
     */
    @Override
    public TypeNode visitNode(final EmptyNode node) {
        if (print) printNode(node);
        return new EmptyTypeNode();
    }

    /**
     * Visit a ClassTypeNode node.
     * Visit all fields and methods.
     * Return null.
     *
     * @param node the ClassTypeNode node to visit.
     * @return null.
     * @throws TypeException if there is a type error.
     */
    @Override
    public TypeNode visitNode(final ClassTypeNode node) throws TypeException {
        if (print) printNode(node);
        // Visit all fields and methods
        for (final TypeNode field : node.fields) visit(field);
        for (final ArrowTypeNode method : node.methods) visit(method);
        return null;
    }

    /**
     * Visit a MethodTypeNode node.
     * Visit all parameters and the return type.
     * Return null.
     *
     * @param node the MethodTypeNode node to visit.
     * @return null.
     * @throws TypeException if there is a type error.
     */
    @Override
    public TypeNode visitNode(final MethodTypeNode node) throws TypeException {
        if (print) printNode(node);
        // Visit all parameters and the return type
        for (final TypeNode parameter : node.functionalType.parameters) visit(parameter);
        visit(node.functionalType.returnType, "->");
        return null;
    }

    /**
     * Visit a RefTypeNode node.
     *
     * @param node the RefTypeNode node to visit.
     * @return null.
     */
    @Override
    public TypeNode visitNode(final RefTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit an EmptyTypeNode node.
     *
     * @param node the EmptyTypeNode node to visit.
     * @return null.
     */
    @Override
    public TypeNode visitNode(final EmptyTypeNode node) {
        if (print) printNode(node);
        return null;
    }

}