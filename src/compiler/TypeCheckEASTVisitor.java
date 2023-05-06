package compiler;

import compiler.AST.*;
import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import static compiler.TypeRels.isSubtype;
import static compiler.TypeRels.superType;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode, TypeException> {

    TypeCheckEASTVisitor() {
        super(true);
    } // enables incomplete tree exceptions

    TypeCheckEASTVisitor(boolean debug) {
        super(true, debug);
    } // enables print for debugging

    //checks that a type object is visitable (not incomplete)
    private TypeNode ckvisit(TypeNode t) throws TypeException {
        visit(t);
        return t;
    }

    // STentry (ritorna campo type)

    @Override
    public TypeNode visitSTentry(STentry entry) throws TypeException {
        if (print) printSTentry("type");
        return ckvisit(entry.type);
    }

    @Override
    public TypeNode visitNode(ProgLetInNode n) throws TypeException {
        if (print) printNode(n);
        for (Node dec : n.decList)
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(ProgNode n) throws TypeException {
        if (print) printNode(n);
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(FunNode n) throws TypeException {
        if (print) printNode(n, n.id);
        for (Node dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
            throw new TypeException("Wrong return type for function " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(VarNode n) throws TypeException {
        if (print) printNode(n, n.id);
        if (!isSubtype(visit(n.exp), ckvisit(n.getType())))
            throw new TypeException("Incompatible value for variable " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(PrintNode n) throws TypeException {
        if (print) printNode(n);
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(IfNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.cond), new BoolTypeNode())))
            throw new TypeException("Non boolean condition in if", n.getLine());
        TypeNode t = visit(n.th);
        TypeNode e = visit(n.el);
        if (isSubtype(t, e)) return e;
        if (isSubtype(e, t)) return t;
        throw new TypeException("Incompatible types in then-else branches", n.getLine());
    }

    @Override
    public TypeNode visitNode(EqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l)))
            throw new TypeException("Incompatible types in equal", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(TimesNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in multiplication", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(PlusNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in sum", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(CallNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if (!(t instanceof ArrowTypeNode at))
            throw new TypeException("Invocation of a non-function " + n.id, n.getLine());
        if (!(at.parlist.size() == n.arglist.size()))
            throw new TypeException("Wrong number of parameters in the invocation of " + n.id, n.getLine());
        for (int i = 0; i < n.arglist.size(); i++)
            if (!(isSubtype(visit(n.arglist.get(i)), at.parlist.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.id, n.getLine());
        return at.ret;
    }

    @Override
    public TypeNode visitNode(IdNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if (t instanceof ArrowTypeNode)
            throw new TypeException("Wrong usage of function identifier " + n.id, n.getLine());
        return t;
    }

    @Override
    public TypeNode visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(BoolTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public TypeNode visitNode(IntTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    // gestione tipi incompleti	(se lo sono lancia eccezione)

    @Override
    public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (Node par : n.parlist) visit(par);
        visit(n.ret, "->"); //marks return type
        return null;
    }

    // ******************
    // ******************
    // OPERATOR EXTENSION
    // ******************
    // ******************

    @Override
    public TypeNode visitNode(MinusNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in minus", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(DivNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in Div", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in Gte", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(LessEqualNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in Lte", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(NotNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.exp), new BoolTypeNode()))) {
            throw new TypeException("Non boolean in not", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(OrNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new BoolTypeNode())
                && isSubtype(visit(n.right), new BoolTypeNode()))) {
            throw new TypeException("Non booleans in or", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(AndNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new BoolTypeNode())
                && isSubtype(visit(n.right), new BoolTypeNode()))) {
            throw new TypeException("Non booleans in and", n.getLine());
        }
        return new BoolTypeNode();
    }


    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    @Override
    public TypeNode visitNode(ClassNode n) throws TypeException {
        if (print) printNode(n, n.classId);
        final boolean isSubClass = n.superId.isPresent();
        final String superId = isSubClass ? n.superId.get() : null;

        // if class has a super class, add it as super type in TypeRels Map
        if (isSubClass) {
            superType.put(n.classId, superId);
        }

        // visit all methods
        for (final MethodNode method : n.methods) {
            try {
                visit(method);
            } catch (TypeException e) {
                System.out.println("Type checking error in a class declaration: " + e.text);
            }
        }

        if (!isSubClass || n.superEntry == null) {
            return null;
        }

        final ClassTypeNode classType = (ClassTypeNode) n.getType();
        final ClassTypeNode parentClassType = (ClassTypeNode) n.superEntry.type;

        // check if all fields and methods of the class are the correct subtypes and with the correct position
        for (final FieldNode field : n.fields) {
            int position = -field.offset - 1;
            if (position < parentClassType.fields.size()
                    && !isSubtype(classType.fields.get(position), parentClassType.fields.get(position))) {
                throw new TypeException("Wrong type for field " + field.fieldId, field.getLine());
            }
        }

        for (final MethodNode method : n.methods) {
            int position = method.offset;
            if (position < parentClassType.methods.size()
                    && !isSubtype(classType.methods.get(position), parentClassType.methods.get(position))) {
                throw new TypeException("Wrong type for method " + method.methodId, method.getLine());
            }
        }

        return null;
    }

    @Override
    public TypeNode visitNode(MethodNode n) throws TypeException {
        if (print) printNode(n, n.methodId);

        for (final DecNode dec : n.declarations)
            try {
                visit(dec);
            } catch (TypeException e) {
                System.out.println("Type checking error in a method declaration: " + e.text);
            }
        // visit expression and check if it is a subtype of the return type
        if (!isSubtype(visit(n.exp), ckvisit(n.returnType))) {
            throw new TypeException("Wrong return type for method " + n.methodId, n.getLine());
        }

        return null;
    }

    @Override
    public TypeNode visitNode(ClassCallNode n) throws TypeException {
        if (print) printNode(n, n.objectId);

        TypeNode type = visit(n.methodEntry);

        // visit method, if it is a method type, get the functional type
        if (type instanceof MethodTypeNode methodTypeNode) {
            type = methodTypeNode.functionalType;
        }

        // if it is not an arrow type, throw an exception
        if (!(type instanceof ArrowTypeNode arrowTypeNode)) {
            throw new TypeException("Invocation of a non-function " + n.methodId, n.getLine());
        }

        // check if the number of parameters is correct
        if (arrowTypeNode.parlist.size() != n.args.size()) {
            throw new TypeException("Wrong number of parameters in the invocation of method " + n.methodId, n.getLine());
        }

        // check if the types of the parameters are correct
        for (int i = 0; i < n.args.size(); i++) {
            if (!(isSubtype(visit(n.args.get(i)), arrowTypeNode.parlist.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of method " + n.methodId, n.getLine());
        }

        return arrowTypeNode.ret;
    }


    @Override
    public TypeNode visitNode(NewNode n) throws TypeException {
        if (print) printNode(n, n.classId);
        final TypeNode t = visit(n.entry);

        if (t instanceof ClassTypeNode node) {
            if (node.fields.size() != n.args.size()) {
                throw new TypeException("Wrong number of parameters in the invocation of constructor " + n.classId, n.getLine());
            }
            // check if the types of the parameters are correct
            for (int i = 0; i < n.args.size(); i++) {
                if (!(isSubtype(visit(n.args.get(i)), node.fields.get(i))))
                    throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of constructor " + n.classId, n.getLine());
            }
        } else {
            throw new TypeException("Invocation of a non-constructor " + n.classId, n.getLine());
        }

        return new RefTypeNode(n.classId);
    }


    @Override
    public TypeNode visitNode(EmptyNode n) {
        if (print) printNode(n);
        return new EmptyTypeNode();
    }

    @Override
    public TypeNode visitNode(ClassTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (final TypeNode field : n.fields) visit(field);
        for (final ArrowTypeNode method : n.methods) visit(method);
        return null;
    }

    @Override
    public TypeNode visitNode(MethodTypeNode node) throws TypeException {
        if (print) printNode(node);
        for (final TypeNode parameter : node.functionalType.parlist) visit(parameter);
        visit(node.functionalType.ret, "->");
        return null;
    }

    @Override
    public TypeNode visitNode(RefTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public TypeNode visitNode(EmptyTypeNode n) {
        if (print) printNode(n);
        return null;
    }

}