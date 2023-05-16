package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import svm.ExecuteVM;

import java.util.ArrayList;
import java.util.List;

import static compiler.CodeGenerationASTVisitor.Instructions.*;
import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

    /**
     * The dispatch tables of the classes.
     * <p>
     * Each dispatch table is a list of labels, one for each method of the class.
     */
    private final List<List<String>> dispatchTables = new ArrayList<>();

    public CodeGenerationASTVisitor() {
        super(false);
    }

    public CodeGenerationASTVisitor(boolean debug) {
        super(false, debug);
    }

    /**
     * Generate code for the ProgLetIn node.
     * <p>
     * The code generated for a ProgLetIn node is:
     * <ul>
     *     <li>the code for the declarations;</li>
     *     <li>the code for the expression;</li>
     *     <li>the halt instruction.</li>
     *     <li>the code generated by the visits with the putCode method.</li>
     *
     * @param node the ProgLetIn node
     * @return the code generated for the ProgLetIn node
     */
    @Override
    public String visitNode(final ProgLetInNode node) {
        if (print) printNode(node);
        String declarationsCode = null;
        for (Node declaration : node.declarations) {
            declarationsCode = nlJoin(declarationsCode, visit(declaration));
        }
        return nlJoin(
                PUSH + 0,
                declarationsCode, // generate code for declarations (allocation)
                visit(node.exp),
                HALT,
                getCode()
        );
    }

    /**
     * Generate code for the ProgNode node.
     * <p>
     * The code generated for a ProgNode node is:
     * <ul>
     *     <li>the code for the expression;</li>
     *     <li>the halt instruction.</li>
     *     <li>the code generated by the visits with the putCode method.</li>
     * </ul>
     *
     * @param node the ProgNode node
     * @return the code generated for the ProgNode node
     */
    @Override
    public String visitNode(final ProgNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.exp),
                HALT
        );
    }

    /**
     * Generate code for the FunNode node.
     * <p>
     * The code generated for a FunNode node is:
     * <ul>
     *   <li>a label for the function;</li>
     *
     *   <li>the code for the declarations;</li>
     *   <li>the code for the expression;</li>
     *
     *   <li>the code for popping the declarations;</li>
     *
     *   <li>the code for popping the parameters;</li>
     *
     * </ul>
     *
     * @param node the FunNode node
     * @return the code generated for the FunNode node
     */
    @Override
    public String visitNode(final FunNode node) {
        if (print) printNode(node, node.id);
        String declarationsCode = null, popDeclarationsCode = null, popParametersCode = null;
        for (Node declaration : node.declarations) {
            declarationsCode = nlJoin(declarationsCode, visit(declaration));
            popDeclarationsCode = nlJoin(popDeclarationsCode, "pop");
        }
        for (int i = 0; i < node.parameters.size(); i++) {
            popParametersCode = nlJoin(popParametersCode, "pop");
        }
        final String funLabel = freshFunLabel();


        putCode(
                nlJoin(
                        funLabel + ":",
                        COPY_FP, // set $fp to $sp value
                        LOAD_RA, // load $ra value
                        declarationsCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(node.exp), // generate code for function body expression
                        STORE_TM, // set $tm to popped value (function result)
                        popDeclarationsCode, // remove local declarations from stack
                        STORE_RA, // set $ra to popped value
                        POP, // remove Access Link from stack
                        popParametersCode, // remove parameters from stack
                        STORE_FP, // set $fp to popped value (Control Link)
                        LOAD_TM, // load $tm value (function result)
                        LOAD_RA, // load $ra value
                        JUMP_STACK // jump to popped address
                )
        );
        return PUSH + funLabel;
    }

    /**
     * Generate code for the VarNode node.
     * <p>
     * The code generated for a VarNode node is:
     * <ul>
     *     <li>the code for the expression;</li>
     * </ul>
     *
     * @param node the VarNode node
     * @return the code generated for the VarNode node
     */
    @Override
    public String visitNode(final VarNode node) {
        if (print) printNode(node, node.id);
        return visit(node.exp);
    }

    /**
     * Generate code for the PrintNode node.
     * <p>
     * The code generated for a PrintNode node is:
     * <ul>
     *     <li>the code for the expression;</li>
     *     <li>the print instruction.</li>
     * </ul>
     *
     * @param node the PrintNode node
     * @return the code generated for the PrintNode node
     */

    @Override
    public String visitNode(final PrintNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.exp),
                PRINT
        );
    }

    /**
     * Generate code for the IfNode node.
     * <p>
     * The code generated for an IfNode node is:
     * <ul>
     *     <li>the code for the condition expression;</li>
     *     <li>the code to check if the condition is true, that jump to the thenLabel</li>
     *     <li>the code for the else branch;</li>
     *     <li>the code to jump to the endLabel;</li>
     *     <li>the thenLabel</li>
     *     <li>the code for the then branch;</li>
     *     <li>the endLabel</li>
     *
     * @param node the IfNode node
     * @return the code generated for the IfNode node
     */
    @Override
    public String visitNode(final IfNode node) {
        if (print) printNode(node);
        String thenLabel = freshLabel();
        String endLabel = freshLabel();
        return nlJoin(
                visit(node.condition),
                PUSH + 1,
                BRANCH_EQUAL + thenLabel,
                visit(node.elseBranch),
                BRANCH + endLabel,
                thenLabel + ":",
                visit(node.thenBranch),
                endLabel + ":"
        );
    }

    /**
     * Generate code for the EqualNode node.
     * <p>
     * The code generated for an EqualNode node is:
     * <ul>
     *     <li>the code for the left expression;</li>
     *     <li>the code for the right expression;</li>
     *     <li>the code to check if the two expressions are equal,
     *         that jump to the trueLabel;</li>
     *     <li>the code to push 0 on the stack (the result of the equal);</li>
     *     <li>the code to jump to the endLabel;</li>
     *     <li>the trueLabel;</li>
     *     <li>the code to push 1 on the stack (the result of the equal);</li>
     *     <li>the endLabel</li>
     *
     * @param node the EqualNode node
     * @return the code generated for the EqualNode node
     */
    @Override
    public String visitNode(final EqualNode node) {
        if (print) printNode(node);
        final String trueLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),
                visit(node.right),
                BRANCH_EQUAL + trueLabel,
                PUSH + 0,
                BRANCH + endLabel,
                trueLabel + ":",
                PUSH + 1,
                endLabel + ":"
        );
    }

    /**
     * Generate code for the TimesNode node.
     * <p>
     * The code generated for a TimesNode node is:
     * <ul>
     *     <li>the code for the left expression;</li>
     *     <li>the code for the right expression;</li>
     *     <li>the code to multiply the two expressions;</li>
     * </ul>
     *
     * @param node the TimesNode node
     * @return the code generated for the TimesNode node
     */
    @Override
    public String visitNode(final TimesNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),
                visit(node.right),
                MULT
        );
    }

    /**
     * Generate code for the PlusNode node.
     * <p>
     * The code generated for a PlusNode node is:
     * <ul>
     *     <li>the code for the left expression;</li>
     *     <li>the code for the right expression;</li>
     *     <li>the code to add the two expressions;</li>
     * </ul>
     *
     * @param node the PlusNode node
     * @return the code generated for the PlusNode node
     */
    @Override
    public String visitNode(final PlusNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),
                visit(node.right),
                ADD
        );
    }

    /**
     * Generate code for the CallNode node.
     * <p>
     * The code generated for a CallNode node is:
     * <ul>
     *     TODO: complete this description
     * </ul>
     *
     * @param node the CallNode node
     * @return the code generated for the CallNode node
     */
    @Override
    public String visitNode(final CallNode node) {
        if (print) printNode(node, node.id);
        String argumentsCode = null, getARCode = null;
        for (int i = node.arguments.size() - 1; i >= 0; i--) {
            argumentsCode = nlJoin(argumentsCode, visit(node.arguments.get(i)));
        }
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) {
            getARCode = nlJoin(getARCode, LOAD_WORD);
        }

        if (node.entry.type instanceof MethodTypeNode methodTypeNode) {
            return nlJoin(
                    LOAD_FP, // load Control Link (pointer to frame of function "id" caller)
                    argumentsCode, // generate code for argument expressions in reversed order
                    LOAD_FP,
                    getARCode, // retrieve address of frame containing "id" declaration
                    // by following the static chain (of Access Links)

                    STORE_TM, // set $tm to popped value (with the aim of duplicating top of stack)
                    LOAD_TM, // load Access Link (pointer to frame of function "id" declaration)
                    LOAD_TM, // duplicate top of stack

                    LOAD_WORD,

                    PUSH + node.entry.offset,
                    ADD, // compute address of "id" declaration
                    LOAD_WORD, // load address of "id" function
                    JUMP_STACK  // jump to popped address (saving address of subsequent instruction in $ra)
            );
        } else {
            return nlJoin(
                    LOAD_FP, // load Control Link (pointer to frame of function "id" caller)
                    argumentsCode, // generate code for argument expressions in reversed order
                    LOAD_FP,
                    getARCode, // retrieve address of frame containing "id" declaration
                    // by following the static chain (of Access Links)
                    STORE_TM, // set $tm to popped value (with the aim of duplicating top of stack)
                    LOAD_TM, // load Access Link (pointer to frame of function "id" declaration)
                    LOAD_TM, // duplicate top of stack
                    PUSH + node.entry.offset,
                    ADD, // compute address of "id" declaration
                    LOAD_WORD, // load address of "id" function
                    JUMP_STACK  // jump to popped address (saving address of subsequent instruction in $ra)
            );
        }
    }

    /**
     * Generate code for the IdNode node.
     * <p>
     * The code generated for an IdNode node is:
     * <ul>
     *     <li>the code to load the address of the frame containing the declaration of the variable;</li>
     *     <li>the code to compute the address of the variable;</li>
     *     <li>the code to load the value of the variable;</li>
     * </ul>
     *
     * @param node the IdNode node
     * @return the code generated for the IdNode node
     */
    @Override
    public String visitNode(final IdNode node) {
        if (print) printNode(node, node.id);
        String getARCode = null;
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) {
            getARCode = nlJoin(getARCode, "lw");
        }
        return nlJoin(
                LOAD_FP, getARCode, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                PUSH + node.entry.offset, ADD, // compute address of "id" declaration
                LOAD_WORD // load value of "id" variable
        );
    }

    /**
     * Generate code for the BoolNode node.
     * <p>
     * The code generated for a BoolNode node is:
     * <ul>
     *     <li>the code to push the value of the boolean;</li>
     * </ul>
     *
     * @param node the BoolNode node
     * @return the code generated for the BoolNode node
     */
    @Override
    public String visitNode(final BoolNode node) {
        if (print) printNode(node, node.value.toString());
        return PUSH + (node.value ? 1 : 0);
    }

    /**
     * Generate code for the IntNode node.
     * <p>
     * The code generated for an IntNode node is:
     * <ul>
     *     <li>the code to push the value of the integer;</li>
     * </ul>
     *
     * @param node the IntNode node
     * @return the code generated for the IntNode node
     */
    @Override
    public String visitNode(final IntNode node) {
        if (print) printNode(node, node.value.toString());
        return PUSH + node.value;
    }

    /**
     * Generate code for the MinusNode node.
     * <p>
     * The code generated for a MinusNode node is:
     * <ul>
     *     <li>the code to generate the left expression;</li>
     *     <li>the code to generate the right expression;</li>
     *     <li>the code to subtract the right value from the left value;</li>
     * </ul>
     *
     * @param node the MinusNode node
     * @return the code generated for the MinusNode node
     */
    @Override
    public String visitNode(final MinusNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),
                visit(node.right),
                SUB
        );
    }

    // ******************
    // ******************
    // OPERATOR EXTENSION
    // ******************
    // ******************

    /**
     * Generate code for the DivNode node.
     * <p>
     * The code generated for a DivNode node is:
     * <ul>
     *     <li>the code to generate the left expression;</li>
     *     <li>the code to generate the right expression;</li>
     *     <li>the code to divide the left value by the right value;</li>
     * </ul>
     *
     * @param node the DivNode node
     * @return the code generated for the DivNode node
     */
    @Override
    public String visitNode(final DivNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),
                visit(node.right),
                DIV
        );
    }

    /**
     * Generate code for the GreaterEqualNode node.
     * <p>
     * The code generated for a GreaterEqualNode node is:
     * <ul>
     *     <li>the code to generate the left expression;</li>
     *     <li>the code to generate the right expression;</li>
     *     <li>the code to subtract 1 from the right value;</li>
     *     <li>the code to compare the left value with the right value;
     *         if the value are not greater or equal, then jump to the false label;</li>
     *     <li>the code to push 1(the result);</li>
     *     <li>the code to jump to the end label;</li>
     *     <li>the false label;</li>
     *     <li>the code to push 0(the result);</li>
     *     <li>the end label;</li>
     * </ul>
     *
     * @param node the GreaterEqualNode node
     * @return the code generated for the GreaterEqualNode node
     */
    @Override
    public String visitNode(final GreaterEqualNode node) {
        if (print) printNode(node);
        final String falseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),
                visit(node.right),
                PUSH + 1,
                SUB,
                BRANCH_LESS_EQUAL + falseLabel,   //if less , then false
                PUSH + 1,
                BRANCH + endLabel,
                falseLabel + ":",       //false
                PUSH + 0,
                endLabel + ":"          //end
        );
    }

    /**
     * Generate code for the LessEqualNode node.
     * <p>
     * The code generated for a LessEqualNode node is:
     * <ul>
     *     <li>the code to generate the left expression;</li>
     *     <li>the code to generate the right expression;</li>
     *     <li>the code to check if the left value is less or equal
     *         than the right value, if so, then jump to the true label;</li>
     *     <li>the code to push 0(the result);</li>
     *     <li>the code to jump to the end label;</li>
     *     <li>the true label;</li>
     *     <li>the code to push 1(the result);</li>
     *     <li>the end label;</li>
     * </ul>
     *
     * @param node the LessEqualNode node
     * @return the code generated for the LessEqualNode node
     */
    @Override
    public String visitNode(final LessEqualNode node) {
        if (print) printNode(node);
        final String endLabel = freshLabel();
        final String trueLabel = freshLabel();
        return nlJoin(
                visit(node.left),
                visit(node.right),
                BRANCH_LESS_EQUAL + trueLabel,    //if less or equal, then true
                PUSH + 0,
                BRANCH + endLabel,
                trueLabel + ":",        //true
                PUSH + 1,
                endLabel + ":"          //end
        );
    }

    /**
     * Generate code for the NotNode node.
     * <p>
     * The code generated for a NotNode node is:
     * <ul>
     *     <li>the code to generate the expression;</li>
     *     <li>the code to check if the value is 0, if so, then jump to the itWasFalseLabel;</li>
     *     <li>the code to push 0(the result);</li>
     *     <li>the code to jump to the end label;</li>
     *     <li>the itWasFalseLabel;</li>
     *     <li>the code to push 1(the result);</li>
     *     <li>the end label;</li>
     *  </ul>
     *
     * @param node the NotNode node
     * @return the code generated for the NotNode node
     */
    @Override
    public String visitNode(final NotNode node) {
        if (print) printNode(node);
        final String itWasFalseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.exp),
                PUSH + 0,
                BRANCH_EQUAL + itWasFalseLabel,
                PUSH + 0,
                BRANCH + endLabel,
                itWasFalseLabel + ":",
                PUSH + 1,
                endLabel + ":"
        );
    }

    /**
     * Generate code for the OrNode node.
     * <p>
     * The code generated for a OrNode node is:
     * <ul>
     *     <li>the code to generate the left expression;</li>
     *     <li>the code to check if the value is 1, if so, then jump to the true label;</li>
     *     <li>the code to generate the right expression;</li>
     *     <li>the code to check if the value is 1, if so, then jump to the true label;</li>
     *     <li>the code to push 0(the result);</li>
     *     <li>the code to jump to the end label;</li>
     *     <li>the true label;</li>
     *     <li>the code to push 1(the result);</li>
     *     <li>the end label;</li>
     * </ul>
     *
     * @param node the OrNode node
     * @return the code generated for the OrNode node
     */
    @Override
    public String visitNode(final OrNode node) {
        if (print) printNode(node);
        final String trueLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),
                PUSH + 1,
                BRANCH_EQUAL + trueLabel,
                visit(node.right),
                PUSH + 1,
                BRANCH_EQUAL + trueLabel,
                PUSH + 0,
                BRANCH + endLabel,
                trueLabel + ":",
                PUSH + 1,
                endLabel + ":"
        );
    }

    /**
     * Generate code for the AndNode node.
     * <p>
     * The code generated for a AndNode node is:
     * <ul>
     *     <li>the code to generate the left expression;</li>
     *     <li>the code to check if the value is 0, if so, then jump to the false label;</li>
     *     <li>the code to generate the right expression;</li>
     *     <li>the code to check if the value is 0, if so, then jump to the false label;</li>
     *     <li>the code to push 1(the result);</li>
     *     <li>the code to jump to the end label;</li>
     *     <li>the false label;</li>
     *     <li>the code to push 0(the result);</li>
     *     <li>the end label;</li>
     * </ul>
     *
     * @param node the AndNode node
     * @return the code generated for the AndNode node
     */
    @Override
    public String visitNode(final AndNode node) {
        if (print) printNode(node);
        final String falseLabel = freshLabel();
        final String endLabel = freshLabel();
        return nlJoin(
                visit(node.left),
                PUSH + 0,
                BRANCH_EQUAL + falseLabel,
                visit(node.right),
                PUSH + 0,
                BRANCH_EQUAL + falseLabel,
                PUSH + 1,
                BRANCH + endLabel,
                falseLabel + ":",
                PUSH + 0,
                endLabel + ":"
        );
    }

    /**
     * Generate code for the ClassNode node.
     * <p>
     * It creates a new dispatch table for the class and adds it to the list of dispatch tables.
     * If the class is a subclass, it copies the dispatch table of the superclass.
     * Then it visits the methods of the class in order to generate the code for them.
     * Finally, it generates the code for the dispatch table.
     * <p>
     * The code generated for a ClassNode node is:
     * <ul>
     *     TODO: add code
     * </ul>
     *
     * @param node the ClassNode node
     * @return the code generated for the ClassNode node
     */
    @Override
    public String visitNode(final ClassNode node) {
        if (print) printNode(node);

        final List<String> dispatchTable = new ArrayList<>();
        dispatchTables.add(dispatchTable);

        final boolean isSubclass = node.superEntry != null;

        if (isSubclass) {
            final List<String> superDispatchTable = dispatchTables.get(-node.superEntry.offset - 2);
            dispatchTable.addAll(superDispatchTable);
        }

        for (final MethodNode methodEntry : node.methods) {
            visit(methodEntry);

            final boolean isOverriding = methodEntry.offset < dispatchTable.size();
            if (isOverriding) {
                dispatchTable.set(methodEntry.offset, methodEntry.label);
            } else {
                dispatchTable.add(methodEntry.label);
            }
        }

        String dispatchTableHeapCode = "";
        for (final String label : dispatchTable) {
            dispatchTableHeapCode = nlJoin(
                    dispatchTableHeapCode,

                    PUSH + label,
                    LOAD_HP,
                    STORE_WORD,

                    LOAD_HP,
                    PUSH + 1,
                    ADD,
                    STORE_HP
            );
        }

        return nlJoin(
                LOAD_HP,
                dispatchTableHeapCode
        );

    }

    // *************************
    // *************************
    // OBJECT-ORIENTED EXTENSION
    // *************************
    // *************************

    /**
     * Generate code for the MethodNode node.
     * <p>
     * It generates the code for the method body adding it to the code with the putCode method.
     *
     * <p>
     * The code generated for a MethodNode node is:
     * <ul>
     *     <li>the method label;</li>
     *     <li>the code to generate the declarations;</li>
     *     <li>the code of the expression;</li>
     *     <li>the code to pop the declarations;</li>
     *     <li>the code to pop the parameters;</li>
     *     <li>the code to return;</li>
     * </ul>
     *
     * @param node the MethodNode node
     * @return the code generated for the MethodNode node
     */
    @Override
    public String visitNode(final MethodNode node) {
        if (print) printNode(node);

        String declarationsCode = "";
        String popDeclarationsCode = "";
        for (final DecNode declaration : node.declarations) {
            declarationsCode = nlJoin(
                    declarationsCode,
                    visit(declaration)
            );
            popDeclarationsCode = nlJoin(
                    popDeclarationsCode,
                    POP
            );
        }

        String popParametersCode = "";
        for (int i = 0; i < node.params.size(); i++) {
            popParametersCode = nlJoin(
                    popParametersCode,
                    POP
            );
        }

        final String methodLabel = freshFunLabel();

        node.label = methodLabel;

        putCode(
                nlJoin(
                        methodLabel + ":",
                        COPY_FP,
                        LOAD_RA,
                        declarationsCode,
                        visit(node.exp),
                        STORE_TM, // set $tm to popped value (function result)
                        popDeclarationsCode,
                        STORE_RA,
                        POP,
                        popParametersCode,
                        STORE_FP,
                        LOAD_TM,
                        LOAD_RA,
                        JUMP_STACK
                )
        );

        return null;
    }

    /**
     * Generate code for the ClassCallNode node.
     * <p>
     * It generates the code for the arguments of the method call and then the code to call the method.
     * <p>
     * The code generated for a ClassCallNode node is:
     * <ul>
     *     <li>the code to generate the arguments;</li>
     *     <li>the code to get the AR;</li>
     *     <li>the code to get the address of the method;</li>
     *     <li>the code to call the method;</li>
     * </ul>
     *
     * @param node the ClassCallNode node
     * @return the code generated for the ClassCallNode node
     */
    @Override
    public String visitNode(final ClassCallNode node) {
        if (print) printNode(node);

        String argumentsCode = "";
        for (int i = node.args.size() - 1; i >= 0; i--) {
            argumentsCode = nlJoin(
                    argumentsCode,
                    visit(node.args.get(i))
            );
        }

        String getARCode = "";
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) {
            getARCode = nlJoin(
                    getARCode,
                    LOAD_WORD
            );
        }

        return nlJoin(
                LOAD_FP,
                argumentsCode,
                LOAD_FP,
                getARCode,

                PUSH + node.entry.offset,
                ADD,

                LOAD_WORD,

                STORE_TM,
                LOAD_TM,
                LOAD_TM,

                LOAD_WORD,

                PUSH + node.methodEntry.offset,
                ADD,

                LOAD_WORD,
                JUMP_STACK
        );

    }

    /**
     * Generate code for the NewNode node.
     * <p>
     * TODO: check this
     * It generates the code for the arguments of the constructor call and then the code to call the constructor.
     * It also generates the code to move the arguments on the heap.
     * <p>
     * The code generated for a NewNode node is:
     * <ul>
     *     <li>the code to generate the arguments;</li>
     *     <li>the code to move the arguments on the heap;</li>
     *     TODO: complete this
     * </ul>
     *
     * @param node the NewNode node
     * @return the code generated for the NewNode node
     */
    @Override
    public String visitNode(final NewNode node) {
        if (print) printNode(node);

        String argumentsCode = "";
        String moveArgumentsOnHeapCode = "";
        for (int i = 0; i < node.args.size(); i++) {
            argumentsCode = nlJoin(
                    argumentsCode,
                    visit(node.args.get(i))
            );
            moveArgumentsOnHeapCode = nlJoin(
                    moveArgumentsOnHeapCode,
                    LOAD_HP,
                    STORE_WORD,
                    LOAD_HP,
                    PUSH + 1,
                    ADD,
                    STORE_HP
            );
        }

        return nlJoin(
                argumentsCode,
                moveArgumentsOnHeapCode,
                PUSH + (ExecuteVM.MEMSIZE + node.entry.offset),
                LOAD_WORD,
                LOAD_HP,
                STORE_WORD,

                LOAD_HP,
                LOAD_HP,
                PUSH + 1,
                ADD,
                STORE_HP
        );

    }

    /**
     * Generate code for the EmptyNode node.
     * <p>
     * The code generated for an EmptyNode node is:
     * <ul>
     *     <li>the code to push -1 on the stack;</li>
     * </ul>
     *
     * @param node the EmptyNode node
     * @return the code generated for the EmptyNode node
     */
    @Override
    public String visitNode(final EmptyNode node) {
        if (print) printNode(node);
        return PUSH + "-1";
    }

    static class Instructions {

        /**
         * Stop the execution of the program.
         */
        static final String HALT = "halt";

        /**
         * Print the value on top of the stack.
         */
        static final String PRINT = "print";

        /**
         * Push the value on the top of the stack.
         */
        static final String PUSH = "push "; // space needed for the argument

        /**
         * Pop the value on the top of the stack.
         */
        static final String POP = "pop";

        /**
         * Add the two values on the top of the stack popping them.
         * The result is pushed on the top of the stack.
         */
        static final String ADD = "add";

        /**
         * Subtract the two values on the top of the stack popping them.
         * The result is pushed on the top of the stack.
         */
        static final String SUB = "sub";

        /**
         * Multiply the two values on the top of the stack popping them.
         * The result is pushed on the top of the stack.
         */
        static final String MULT = "mult";

        /**
         * Divide the two values on the top of the stack popping them.
         * The result is pushed on the top of the stack.
         */
        static final String DIV = "div";

        /**
         * Jump to the label passed as argument.
         */
        static final String BRANCH = "b "; // space needed for the argument

        /**
         * Jump to the label passed as argument if the
         * two values on the top of the stack are equal.
         * <p>
         * The two values are popped.
         */
        static final String BRANCH_EQUAL = "beq "; // space needed for the argument

        /**
         * Jump to the label passed as argument if the
         * first value on the top of the stack is less or
         * equal than the second value on the top of the stack.
         * <p>
         * The two values are popped.
         */
        static final String BRANCH_LESS_EQUAL = "ble "; // space needed for the argument

        /**
         * Push the value of FP on the top of the stack.
         */
        static final String LOAD_FP = "lfp";

        /**
         * Pop the value on the top of the stack and
         * store it in FP.
         */
        static final String STORE_FP = "sfp";

        /**
         * Copy the value of SP in FP.
         */
        static final String COPY_FP = "cfp";

        /**
         * Push the value of RA on the top of the stack.
         */
        static final String LOAD_RA = "lra";

        /**
         * Pop the value on the top of the stack and
         * store it in RA.
         */
        static final String STORE_RA = "sra";

        /**
         * Push the value of TM on the top of the stack.
         */
        static final String LOAD_TM = "ltm";

        /**
         * Pop the value on the top of the stack and
         * store it in TM.
         */
        static final String STORE_TM = "stm";

        /**
         * Push the value of HP on the top of the stack.
         */
        static final String LOAD_HP = "lhp";

        /**
         * Pop the value on the top of the stack and
         * store it in HP.
         */
        static final String STORE_HP = "shp";

        /**
         * Pop the address on the top of the stack and
         * push the value stored at that address.
         */
        static final String LOAD_WORD = "lw";

        /**
         * Pop the address on the top of the stack and
         * pop the value to store at that address.
         */
        static final String STORE_WORD = "sw";

        /**
         * Set the RETURN ADDRESS to the actual INSTRUCTION POINTER.
         * JUMP to the address on the top of the stack.
         */
        static final String JUMP_STACK = "js";


    }

}