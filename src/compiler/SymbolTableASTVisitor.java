package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.TypeNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements the linking phase of the compiler.
 * It uses the ASTVisitor to visit the AST and build the symbol table, and the class table.
 * <p>
 * The symbol table is used to link the identifiers to their declarations.
 * <p>
 * The AST after the visit is called Enriched AST.
 */
public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    /**
     * The classTable is used to store the virtual table of each class
     */
    private final Map<String, VirtualTable> classTable = new HashMap<>();

    /**
     * The symbolTable is used to store the symbol table of each scope
     */
    private final List<Map<String, STentry>> symbolTable = new ArrayList<>();

    int stErrors = 0;

    /**
     * The nestingLevel is used to keep track of the current nesting level
     */
    private int nestingLevel = 0;

    /**
     * The decOffset is used to keep track of the offset of the local declarations
     * at the current nesting level.
     * It is initialized to -2 because the $fp point to the first argument of the function (offset 0)
     * and the next element on the stack is the return address that have offset -1.
     */
    private int decOffset = -2;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(final boolean debug) {
        super(true, debug);
    }

    /**
     * Do a lookup in the symbol table for the given id.
     * The lookup starts from the current nesting level and goes up
     * until the first occurrence of id is found.
     * If no entry is found, null is returned.
     * If an entry is found, the entry is returned.
     *
     * @param id the id to look for
     * @return the entry found or null
     */
    private STentry stLookup(final String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null)
            entry = symbolTable.get(j--).get(id);
        return entry;
    }

    /* *******************
     *********************
     * Main program nodes
     *********************
     ******************* */

    /**
     * Visit a ProgLetInNode.
     * A new scope is created and the declarations are visited.
     * Then the expression is visited.
     * The scope is then removed.
     *
     * @param node the ProgLetInNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final ProgLetInNode node) {
        if (print) printNode(node);
        symbolTable.add(new HashMap<>());
        node.declarations.forEach(this::visit);
        visit(node.exp);
        symbolTable.remove(0);
        return null;
    }

    /**
     * Visit a ProgNode.
     * The expression is visited.
     *
     * @param node the ProgNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final ProgNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    /* *******************
     *********************
     * Basic Declaration Nodes
     *********************
     ******************* */

    /**
     * Visit a FunNode.
     * Create the STentry for the function and add it to the current symbol table.
     * Create a new scope for the function parameters and add them to the new symbol table.
     * Then visit each function declarations and body and remove the scope.
     *
     * @param node the FunNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final FunNode node) {
        if (print) printNode(node);

        final Map<String, STentry> currentSymbolTable = symbolTable.get(nestingLevel);
        final List<TypeNode> parametersTypes = node.parameters.stream()
                .map(ParNode::getType)
                .collect(Collectors.toList());
        final ArrowTypeNode arrowTypeNode = new ArrowTypeNode(parametersTypes, node.returnType);
        node.setType(arrowTypeNode);
        final STentry entry = new STentry(nestingLevel, arrowTypeNode, decOffset--);

        // inserimento di ID nella symtable
        if (currentSymbolTable.put(node.id, entry) != null) {
            System.out.println("Fun id " + node.id + " at line " + node.getLine() + " already declared");
            stErrors++;
        }

        // region Inner Scope for function parameters
        // creare una nuova hashmap per la symTable
        nestingLevel++;
        int prevDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2; // reinitialize counter for offset of declarations at current nesting level
        final Map<String, STentry> newSymbolTable = new HashMap<>();
        symbolTable.add(newSymbolTable);

        int parOffset = 1;
        for (ParNode par : node.parameters) {
            final STentry parEntry = new STentry(nestingLevel, par.getType(), parOffset++);
            if (newSymbolTable.put(par.id, parEntry) != null) {
                System.out.println("Par id " + par.id + " at line " + node.getLine() + " already declared");
                stErrors++;
            }
        }
        node.declarations.forEach(this::visit);
        visit(node.exp);
        // rimuovere la hashmap corrente poiche' esco dallo scope
        symbolTable.remove(nestingLevel);
        decOffset = prevDecOffset; // restores counter for offset of declarations at previous nesting level
        nestingLevel--;
        // endregion
        return null;
    }

    /**
     * Visit a VarNode.
     * Create the STentry for the variable and add it to the current symbol table.
     * Visit the expression.
     *
     * @param node the VarNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final VarNode node) {
        if (print) printNode(node);
        visit(node.exp);
        final Map<String, STentry> currentSymbolTable = symbolTable.get(nestingLevel);
        final STentry entry = new STentry(nestingLevel, node.getType(), decOffset--);
        // inserimento di ID nella symtable
        if (currentSymbolTable.put(node.id, entry) != null) {
            System.out.println("Var id " + node.id + " at line " + node.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    /* *******************
     *********************
     * Operators Nodes
     *********************
     ******************* */

    /**
     * Visit an IfNode.
     * Visit the condition, then branch and else branch.
     *
     * @param node the IfNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final IfNode node) {
        if (print) printNode(node);
        visit(node.condition);
        visit(node.thenBranch);
        visit(node.elseBranch);
        return null;
    }

    /**
     * Visit a NotNode.
     * Visit the expression.
     *
     * @param node the NotNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final NotNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    /**
     * Visit a OrNode.
     * Visit the left and right expression.
     *
     * @param node the OrNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final OrNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a AndNode.
     * Visit the left and right expression.
     *
     * @param node the AndNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final AndNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit an EqualNode.
     * Visit the left and right expression.
     *
     * @param node the EqualNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final EqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a LessEqualNode.
     * Visit the left and right expression.
     *
     * @param node the LessEqualNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final LessEqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a GreaterEqualNode.
     * Visit the left and right expression.
     *
     * @param node the GreaterEqualNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final GreaterEqualNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a TimesNode.
     * Visit the left and right expression.
     *
     * @param node the TimesNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final TimesNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a DivNode.
     * Visit the left and right expression.
     *
     * @param node the DivNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final DivNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a PlusNode.
     * Visit the left and right expression.
     *
     * @param node the PlusNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final PlusNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /**
     * Visit a MinusNode.
     * Visit the left and right expression.
     *
     * @param node the MinusNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final MinusNode node) {
        if (print) printNode(node);
        visit(node.left);
        visit(node.right);
        return null;
    }

    /* *******************
     *********************
     * Values Nodes
     *********************
     ******************* */

    /**
     * Visit a BoolNode.
     *
     * @param node the BoolNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final BoolNode node) {
        if (print) printNode(node, String.valueOf(node.value));
        return null;
    }

    /**
     * Visit a IntNode.
     *
     * @param node the IntNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final IntNode node) {
        if (print) printNode(node, node.value.toString());
        return null;
    }

    /**
     * Visit a IdNode.
     * Lookup the variable in the symbol table and set the entry and nesting level.
     *
     * @param node the IdNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final IdNode node) {
        if (print) printNode(node);
        final STentry entry = stLookup(node.id);
        if (entry == null) {
            System.out.println("Var or Par id " + node.id + " at line " + node.getLine() + " not declared");
            stErrors++;
        } else {
            node.entry = entry;
            node.nestingLevel = nestingLevel;
        }
        return null;
    }

    /* *******************
     *********************
     * Operations Nodes
     *********************
     ******************* */

    /**
     * Visit a PrintNode.
     * Visit the expression.
     *
     * @param node the PrintNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final PrintNode node) {
        if (print) printNode(node);
        visit(node.exp);
        return null;
    }

    /**
     * Visit a CallNode.
     * Lookup the function in the symbol table and set the entry and nesting level.
     * Visit the arguments.
     *
     * @param node the CallNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final CallNode node) {
        if (print) printNode(node);
        final STentry entry = stLookup(node.id);
        if (entry == null) {
            System.out.println("Fun id " + node.id + " at line " + node.getLine() + " not declared");
            stErrors++;
        } else {
            node.entry = entry;
            node.nestingLevel = nestingLevel;
        }
        node.arguments.forEach(this::visit);
        return null;
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
     * Visit a ClassNode.
     * Check if the super class is declared and set the super entry.
     * Create a new ClassTypeNode and set the super class fields and methods if present.
     * Create the STentry and add it to the global symbol table.
     * Create the Virtual Table inheriting the super class methods if present.
     * Add the Virtual Table to the class table.
     * Add the new symbol table for methods and fields.
     * For each field, visit it and create the STentry, also enriching the classTypeNode.
     * For each method, enrich the classTypeNode and visit it.
     * Remove the symbol table for methods and fields and restore the nesting level.
     *
     * @param node the ClassNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final ClassNode node) {
        if (print) printNode(node);

        ClassTypeNode tempClassTypeNode = new ClassTypeNode();
        final boolean isSubClass = node.superId.isPresent();
        final String superId = isSubClass ? node.superId.get() : null;

        if (isSubClass) {
            // Check if the super class is declared
            if (classTable.containsKey(superId)) {
                final STentry superSTEntry = symbolTable.get(0).get(superId);
                final ClassTypeNode superTypeNode = (ClassTypeNode) superSTEntry.type;
                tempClassTypeNode = new ClassTypeNode(superTypeNode);
                node.superEntry = superSTEntry;
            } else {
                System.out.println("Class " + superId + " at line " + node.getLine() + " not declared");
                stErrors++;
            }
        }

        final ClassTypeNode classTypeNode = tempClassTypeNode;
        node.type = classTypeNode;

        // Add the class id to the global scope table checking for duplicates
        final STentry entry = new STentry(0, classTypeNode, decOffset--);
        final Map<String, STentry> globalScopeTable = symbolTable.get(0);
        if (globalScopeTable.put(node.classId, entry) != null) {
            System.out.println("Class id " + node.classId + " at line " + node.getLine() + " already declared");
            stErrors++;
        }

        // Add the class to the class table
        final Set<String> visitedClassNames = new HashSet<>();
        final VirtualTable virtualTable = new VirtualTable();
        if (isSubClass) {
            final VirtualTable superClassVirtualTable = classTable.get(superId);
            virtualTable.putAll(superClassVirtualTable);
        }
        classTable.put(node.classId, virtualTable);

        symbolTable.add(virtualTable);
        // Setting the field offset
        nestingLevel++;
        int fieldOffset = -1;
        if (isSubClass) {
            final ClassTypeNode superTypeNode = (ClassTypeNode) symbolTable.get(0).get(superId).type;
            fieldOffset = -superTypeNode.fields.size() - 1;
        }

        /*
         * Handle field declaration.
         */
        for (final FieldNode field : node.fields) {
            if (visitedClassNames.contains(field.fieldId)) {
                System.out.println(
                        "Field with id " + field.fieldId + " on line " + field.getLine() + " was already declared"
                );
                stErrors++;
            } else {
                visitedClassNames.add(field.fieldId);
            }
            visit(field);

            STentry fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset--);
            final boolean isFieldOverridden = isSubClass && virtualTable.containsKey(field.fieldId);
            if (isFieldOverridden) {
                final STentry overriddenFieldEntry = virtualTable.get(field.fieldId);
                final boolean isOverridingAMethod = overriddenFieldEntry.type instanceof MethodTypeNode;
                if (isOverridingAMethod) {
                    System.out.println("Cannot override method " + field.fieldId + " with a field");
                    stErrors++;
                } else {
                    fieldEntry = new STentry(nestingLevel, field.getType(), overriddenFieldEntry.offset);
                    classTypeNode.fields.set(-fieldEntry.offset - 1, fieldEntry.type);
                }
            } else {
                classTypeNode.fields.add(-fieldEntry.offset - 1, fieldEntry.type);
            }

            // Add the field to the virtual table
            virtualTable.put(field.fieldId, fieldEntry);
            field.offset = fieldEntry.offset;
        }

        // Setting the method offset
        int prevDecOffset = decOffset;
        decOffset = 0;
        if (isSubClass) {
            final ClassTypeNode superTypeNode = (ClassTypeNode) symbolTable.get(0).get(superId).type;
            decOffset = superTypeNode.methods.size();
        }

        for (final MethodNode method : node.methods) {
            if (visitedClassNames.contains(method.methodId)) {
                System.out.println(
                        "Method with id " + method.methodId + " on line " + method.getLine() + " was already declared"
                );
                stErrors++;
            } else {
                visitedClassNames.add(method.methodId);
            }
            visit(method);
            final MethodTypeNode methodTypeNode = (MethodTypeNode) symbolTable.get(nestingLevel).get(method.methodId).type;
            classTypeNode.methods.add(
                    method.offset,
                    methodTypeNode.functionalType
            );
        }

        // Remove the class from the symbol table
        symbolTable.remove(nestingLevel--);
        decOffset = prevDecOffset;
        return null;
    }

    /**
     * Visit a FieldNode.
     *
     * @param node the FieldNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final FieldNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a MethodNode.
     * Create the MethodTypeNode and the STentry adding it to the symbol table.
     * If the method is overriding another method, check if the overriding is correct.
     * Create the new SymbolTable for the method scope.
     * For each parameter, create the STentry and add it to the symbol table.
     * Visit the declarations and the expression.
     * Finally, remove the method scope from the symbol table.
     *
     * @param node the MethodNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final MethodNode node) {
        if (print) printNode(node);
        final Map<String, STentry> currentTable = symbolTable.get(nestingLevel);
        final List<TypeNode> params = node.params.stream()
                .map(ParNode::getType)
                .toList();
        final boolean isOverriding = currentTable.containsKey(node.methodId);
        final TypeNode methodType = new MethodTypeNode(new ArrowTypeNode(params, node.returnType));
        STentry entry = new STentry(nestingLevel, methodType, decOffset++);
        if (isOverriding) {
            final var overriddenMethodEntry = currentTable.get(node.methodId);
            final boolean isOverridingAMethod = overriddenMethodEntry != null && overriddenMethodEntry.type instanceof MethodTypeNode;
            if (isOverridingAMethod) {
                entry = new STentry(nestingLevel, methodType, overriddenMethodEntry.offset);
                decOffset--;
            } else {
                System.out.println("Cannot override a class attribute with a method: " + node.methodId);
                stErrors++;
            }
        }

        node.offset = entry.offset;
        currentTable.put(node.methodId, entry);

        // Create a new table for the method.
        nestingLevel++;
        final Map<String, STentry> methodTable = new HashMap<>();
        symbolTable.add(methodTable);

        // Set the declaration offset
        int prevDecOffset = decOffset;
        decOffset = -2;
        int parameterOffset = 1;

        for (final ParNode parameter : node.params) {
            final STentry parameterEntry = new STentry(nestingLevel, parameter.getType(), parameterOffset++);
            if (methodTable.put(parameter.id, parameterEntry) != null) {
                System.out.println("Par id " + parameter.id + " at line " + node.getLine() + " already declared");
                stErrors++;
            }
        }
        node.declarations.forEach(this::visit);
        visit(node.exp);

        // Remove the current nesting level symbolTable.
        symbolTable.remove(nestingLevel--);
        decOffset = prevDecOffset;
        return null;
    }

    /* *******************
     *********************
     * Value Nodes
     *********************
     ******************* */

    /**
     * Visit an EmptyNode.
     *
     * @param node the EmptyNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final EmptyNode node) {
        if (print) printNode(node);
        return null;
    }

    /* *******************
     *********************
     * Operations Nodes
     *********************
     ******************* */

    /**
     * Visit a ClassCallNode.
     * Check if the object id was declared doing a lookup in the symbol table.
     * If the object id was not declared, print an error.
     * If the object id was declared, check if the type is a RefTypeNode.
     * If the type is not a RefTypeNode, print an error.
     * If the type is a RefTypeNode, check if the method id is in the virtual table.
     * If the method id is not in the virtual table, print an error.
     * If the method id is in the virtual table, set the entry and the nesting level of the node.
     * Finally, visit the arguments.
     *
     * @param node the ClassCallNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final ClassCallNode node) {
        if (print) printNode(node);
        final STentry entry = stLookup(node.objectId);
        if (entry == null) {
            System.out.println("Object id " + node.objectId + " was not declared");
            stErrors++;
        } else if (entry.type instanceof final RefTypeNode refTypeNode) {
            node.entry = entry;
            node.nestingLevel = nestingLevel;
            final VirtualTable virtualTable = classTable.get(refTypeNode.typeId);
            if (virtualTable.containsKey(node.methodId)) {
                node.methodEntry = virtualTable.get(node.methodId);
            } else {
                System.out.println(
                        "Object id " + node.objectId + " at line " + node.getLine() + " has no method " + node.methodId
                );
                stErrors++;
            }
        } else {
            System.out.println("Object id " + node.objectId + " at line " + node.getLine() + " is not a RefType");
            stErrors++;
        }
        node.args.forEach(this::visit);
        return null;
    }

    /**
     * Visit a NewNode.
     * Check if the class id was declared doing a lookup in the class table.
     * If the class id was not declared, print an error.
     * If the class id was declared, set the entry of the node.
     * Finally, visit the arguments.
     *
     * @param node the NewNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final NewNode node) {
        if (print) printNode(node);
        if (!classTable.containsKey(node.classId)) {
            System.out.println("Class id " + node.classId + " was not declared");
            stErrors++;
        }
        node.entry = symbolTable.get(0).get(node.classId);
        node.args.forEach(this::visit);
        return null;
    }

    /* *******************
     *********************
     * OOP Type Nodes
     *********************
     ******************* */

    /**
     * Visit a ClassTypeNode.
     *
     * @param node the ClassTypeNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final ClassTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a MethodTypeNode.
     *
     * @param node the MethodTypeNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final MethodTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * Visit a RefTypeNode.
     * Check if the type id was declared doing a lookup in the class table.
     * If the type id was not declared, print an error.
     *
     * @param node the RefTypeNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final RefTypeNode node) {
        if (print) printNode(node);
        if (!this.classTable.containsKey(node.typeId)) {
            System.out.println("Class with id: " + node.typeId + " on line: " + node.getLine() + " was not declared");
            stErrors++;
        }
        return null;
    }

    /**
     * Visit an EmptyTypeNode.
     *
     * @param node the EmptyTypeNode to visit
     * @return null
     */
    @Override
    public Void visitNode(final EmptyTypeNode node) {
        if (print) printNode(node);
        return null;
    }

    /**
     * The VirtualTable class.
     * It is a Map that maps a method id to a STentry.
     * This is used as an alias for the HashMap<String, STentry> class.
     */
    static class VirtualTable extends HashMap<String, STentry> {
    }

}
