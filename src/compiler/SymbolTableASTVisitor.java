package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import java.util.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private final Map<String, VirtualTable> classTable = new HashMap<>();
    private final List<Map<String, STentry>> symbolTable = new ArrayList<>();
    int stErrors = 0;
    Set<String> onClassVisitScope;
    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(true, debug);
    } // enables print for debugging

    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null)
            entry = symbolTable.get(j--).get(id);
        return entry;
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = new HashMap<>();
        symbolTable.add(hm);
        for (Node dec : n.decList) visit(dec);
        visit(n.exp);
        symbolTable.remove(0);
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symbolTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        //creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symbolTable.add(hmn);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symbolTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        Map<String, STentry> hm = symbolTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Var id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        if (print) printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(MinusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    // ******************
    // ******************
    // OPERATOR EXTENSION
    // ******************
    // ******************

    @Override
    public Void visitNode(DivNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }


    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************


    @Override
    public Void visitNode(ClassNode node) {
        if (print) {
            printNode(node);
        }
        //var classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
        /*
         * Class is extending
         */
        ClassTypeNode tempClassTypeNode = new ClassTypeNode();
        final boolean isSubClass = node.superId.isPresent();
        final String superId = isSubClass ? node.superId.get() : null;
        if (isSubClass) {


            // Check if the super class is declared
            if (classTable.containsKey(node.superId.get())) {
                final STentry superSTEntry = symbolTable.get(0).get(node.superId.get());

                final ClassTypeNode superTypeNode = (ClassTypeNode) superSTEntry.type;
                tempClassTypeNode = new ClassTypeNode(superTypeNode);
            } else {
                System.out.println("Class " + node.superId.get() + " at line " + node.getLine() + " not declared");
                stErrors++;
            }
        }
        final ClassTypeNode classTypeNode = tempClassTypeNode;
        node.type = classTypeNode;

        STentry entry = new STentry(0, classTypeNode, decOffset--);
        node.type = classTypeNode;
        Map<String, STentry> globalScopeTable = symbolTable.get(0);
        if (globalScopeTable.put(node.classId, entry) != null) {
            System.out.println("Class id " + node.classId + " at line " + node.getLine() + " already declared");
            stErrors++;
        }
        /*
         * Add a the scope table for the id of the class.
         * Table should be added for both symbol table and class table.
         */
        nestingLevel++;
        onClassVisitScope = new HashSet<>();
        VirtualTable virtualTable = new VirtualTable();
        //Map<String, STentry> virtualTable = new HashMap<>();


        if (isSubClass) {

            var superClassVirtualTable = classTable.get(superId);
            virtualTable.putAll(superClassVirtualTable);
        }
        classTable.put(node.classId, virtualTable);
        symbolTable.add(virtualTable);
        /*
         * Setting the fieldOffset for the extending class
         */
        int fieldOffset = -1;
        if (isSubClass) {
            final ClassTypeNode superTypeNode = (ClassTypeNode) symbolTable.get(0).get(node.superId.get()).type;
            fieldOffset = -superTypeNode.fields.size() - 1;
        }
        /*
         * Handle field declaration.
         */
        for (var field : node.fields) {
            if (onClassVisitScope.contains(field.fieldId)) {
                System.out.println(
                        "Field with id " + field.fieldId + " on line " + field.getLine() + " was already declared"
                );
                stErrors++;
            }
            onClassVisitScope.add(field.fieldId);
            var overriddenFieldEntry = virtualTable.get(field.fieldId);
            STentry fieldEntry;
            if (overriddenFieldEntry != null && !(overriddenFieldEntry.type instanceof MethodTypeNode)) {
                fieldEntry = new STentry(nestingLevel, field.getType(), overriddenFieldEntry.offset);
                classTypeNode.fields.set(-fieldEntry.offset - 1, fieldEntry.type);
            } else {
                fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset--);
                classTypeNode.fields.add(-fieldEntry.offset - 1, fieldEntry.type);
                if (overriddenFieldEntry != null) {
                    System.out.println("Cannot override field id " + field.fieldId + " with a method");
                    stErrors++;
                }
            }
            /*
             * Add field id in symbol(virtual) table
             */
            virtualTable.put(field.fieldId, fieldEntry);
            field.offset = fieldEntry.offset;
        }
        int currentDecOffset = decOffset;
        // method decOffset starts from 0
        int previousNestingLeveldecOffset = decOffset;
        decOffset = 0;
        if (isSubClass) {
            decOffset = ((ClassTypeNode) symbolTable.get(0).get(superId).type).methods.size();
        }
        for (var method : node.methods) {
            if (onClassVisitScope.contains(method.methodId)) {
                System.out.println(
                        "Method with id " + method.methodId + " on line " + method.getLine() + " was already declared"
                );
                stErrors++;
            }
            visit(method);
            classTypeNode.methods.add(
                    method.offset,
                    ((MethodTypeNode) virtualTable.get(method.methodId).type).functionalType
            );
        }
        decOffset = currentDecOffset; // restores the previous declaration offset
        symbolTable.remove(nestingLevel--);
        decOffset = previousNestingLeveldecOffset;
        return null;
    }

    @Override
    public Void visitNode(MethodNode node) {
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
        int prevdecOffset = decOffset;
        decOffset = -2;
        int parameterOffset = 1;

        for (ParNode parameter : node.params) {
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
        decOffset = prevdecOffset;
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode node) {
        if (print) printNode(node);
        final STentry entry = stLookup(node.objectId);
        if (entry == null) {
            System.out.println("Object id " + node.objectId + " was not declared");
            stErrors++;
        } else if (entry.type instanceof final RefTypeNode refTypeNode) {
            node.entry = entry;
            node.nestingLevel = nestingLevel;
            final VirtualTable virtualTable = classTable.get(refTypeNode.typeId);
            //System.out.println(classTable);
            //System.out.println("Virtual table for " + refTypeNode.typeId + ": " + virtualTable);
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

    @Override
    public Void visitNode(NewNode node) {
        if (print) printNode(node);
        if (!classTable.containsKey(node.classId)) {
            System.out.println("Class id " + node.classId + " was not declared");
            stErrors++;
        }
        node.entry = symbolTable.get(0).get(node.classId);
        node.args.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(RefTypeNode node) {
        if (print) printNode(node);
        if (!this.classTable.containsKey(node.typeId)) {
            System.out.println("Class with id: " + node.typeId + " on line: " + node.getLine() + " was not declared");
            stErrors++;
        }
        return null;
    }

    @Override
    public Void visitNode(FieldNode node) {
        if (print) printNode(node);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public Void visitNode(ClassTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public Void visitNode(MethodTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public Void visitNode(EmptyTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    static class VirtualTable extends HashMap<String, STentry> {
    }

}
