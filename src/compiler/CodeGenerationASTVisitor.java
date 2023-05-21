package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import svm.ExecuteVM;

import java.util.ArrayList;
import java.util.Collections;
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

    /* *******************
     *********************
     * Main program nodes
     *********************
     ******************* */

    /**
     * Generate code for the ProgLetIn node.
     *
     * @param node the ProgLetIn node
     * @return the code generated for the ProgLetIn node
     */
    @Override
    public String visitNode(final ProgLetInNode node) {
        if (print) printNode(node);
        String declarationsCode = null;
        for (final Node declaration : node.declarations) {
            declarationsCode = nlJoin(declarationsCode, visit(declaration));
        }
        return nlJoin(
                PUSH + 0,    // Fake return address for the main
                declarationsCode,   // generate code for declarations (allocation)
                visit(node.exp),    // generate code for the expression
                HALT,               // halt instruction
                getCode()           // generated code for functions
        );
    }

    /**
     * Generate code for the ProgNode node.
     *
     * @param node the ProgNode node
     * @return the code generated for the ProgNode node
     */
    @Override
    public String visitNode(final ProgNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.exp),    // generate code for the expression
                HALT                // halt instruction
        );
    }

    /* *******************
     *********************
     * Basic Declaration Nodes
     *********************
     ******************* */

    /**
     * Generate code for the FunNode node.
     *
     * @param node the FunNode node
     * @return the code generated for the FunNode node
     */
    @Override
    public String visitNode(final FunNode node) {
        if (print) printNode(node, node.id);

        String declarationsCode = null;
        for (final Node declaration : node.declarations) {
            declarationsCode = nlJoin(declarationsCode, visit(declaration));
        }

        String popDeclarationsCode = null;
        for (final Node declaration : node.declarations) {
            popDeclarationsCode = nlJoin(popDeclarationsCode, POP);
        }

        String popParametersCode = null;
        for (final ParNode parameter : node.parameters) {
            popParametersCode = nlJoin(popParametersCode, POP);
        }

        final String funLabel = freshFunLabel();
        putCode(
                nlJoin(
                        funLabel + ":",

                        // Complete stack setup
                        COPY_FP,                // set $fp to $sp value
                        LOAD_RA,                // push $ra value (return address)
                        declarationsCode,       // generate code for local declarations (they use the new $fp)

                        // Function body
                        visit(node.exp),        // generate code for function body expression,
                        // it pushes the result on the stack


                        // Clean up the stack frame
                        STORE_TM,               // set $tm to popped value (function result)
                        popDeclarationsCode,    // remove local declarations from stack
                        STORE_RA,               // set $ra to popped value (return address)
                        POP,                    // remove Access Link from stack
                        popParametersCode,      // remove parameters from stack
                        STORE_FP,               // set $fp to popped value (Control Link (pointer to frame of function "id" caller))

                        // Return
                        LOAD_TM,                // push $tm value (function result)
                        LOAD_RA,                // push $ra value (return address)
                        JUMP_SUBROUTINE              // jump to popped address (return address)
                )
        );

        return PUSH + funLabel; // push function label
    }

    /**
     * Generate code for the VarNode node.
     *
     * @param node the VarNode node
     * @return the code generated for the VarNode node
     */
    @Override
    public String visitNode(final VarNode node) {
        if (print) printNode(node, node.id);
        return visit(node.exp); // generate code for the expression
    }

    /* *******************
     *********************
     * Operators Nodes
     *********************
     ******************* */

    /**
     * Generate code for the IfNode node.
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
                visit(node.condition),   // generate code for the condition expression
                PUSH + 1,                       // push 1 on the stack
                BRANCH_EQUAL + thenLabel,       // jump to thenLabel if the condition is true
                visit(node.elseBranch),         // generate code for the else branch
                BRANCH + endLabel,              // jump to endLabel
                thenLabel + ":",                // thenLabel
                visit(node.thenBranch),         // generate code for the then branch
                endLabel + ":"                  // endLabel
        );
    }

    /**
     * Generate code for the NotNode node.
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
                visit(node.exp),          // generate code for expression
                PUSH + 0,                       // push 0
                BRANCH_EQUAL + itWasFalseLabel, // if value is 0, jump to itWasFalseLabel
                PUSH + 0,                       // push 0 (the result)
                BRANCH + endLabel,              // jump to end label
                itWasFalseLabel + ":",          // itWasFalseLabel
                PUSH + 1,                       // push 1 (the result)
                endLabel + ":"                  // end label
        );
    }

    /**
     * Generate code for the OrNode node.
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
                visit(node.left),    // generate code for left expression
                PUSH + 1,                   // push 1
                BRANCH_EQUAL + trueLabel,   // if value is 1, jump to true label
                visit(node.right),          // generate code for right expression
                PUSH + 1,                   // push 1
                BRANCH_EQUAL + trueLabel,   // if value is 1, jump to true label
                PUSH + 0,                   // push 0 (the result)
                BRANCH + endLabel,          // jump to end label
                trueLabel + ":",            // true label
                PUSH + 1,                   // push 1 (the result)
                endLabel + ":"              // end label
        );
    }

    /**
     * Generate code for the AndNode node.
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
                visit(node.left),    // generate code for left expression
                PUSH + 0,                   // push 0
                BRANCH_EQUAL + falseLabel,  // if value is 0, jump to false label
                visit(node.right),          // generate code for right expression
                PUSH + 0,                   // push 0
                BRANCH_EQUAL + falseLabel,  // if value is 0, jump to false label
                PUSH + 1,                   // push 1 (the result)
                BRANCH + endLabel,          // jump to end label
                falseLabel + ":",           // false label
                PUSH + 0,                   // push 0 (the result)
                endLabel + ":"              // end label
        );
    }

    /**
     * Generate code for the EqualNode node.
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
                visit(node.left),    // generate code for the left expression
                visit(node.right),          // generate code for the right expression
                BRANCH_EQUAL + trueLabel,   // jump to trueLabel if the two expressions are equal
                PUSH + 0,                   // push 0 on the stack (the result of the equal)
                BRANCH + endLabel,          // jump to endLabel
                trueLabel + ":",            // trueLabel
                PUSH + 1,                   // push 1 on the stack (the result of the equal)
                endLabel + ":"              // endLabel
        );
    }

    /**
     * Generate code for the MinusNode node.
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
                visit(node.left),        // generate code for left expression
                visit(node.right),              // generate code for right expression
                BRANCH_LESS_EQUAL + trueLabel,  // if left value is less or equal than right value, jump to true label
                PUSH + 0,                       // push 0 (the result)
                BRANCH + endLabel,              // jump to end label
                trueLabel + ":",                // true label
                PUSH + 1,                       // push 1 (the result)
                endLabel + ":"                  // end label
        );
    }

    /**
     * Generate code for the GreaterEqualNode node.
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
                visit(node.left),        // generate code for left expression
                visit(node.right),              // generate code for right expression
                PUSH + 1,                       // push 1
                SUB,                            // subtract 1 from right value
                BRANCH_LESS_EQUAL + falseLabel, // if left value is not less or equal than right value, jump to false label
                PUSH + 1,                       // push 1 (the result)
                BRANCH + endLabel,              // jump to end label
                falseLabel + ":",               // false label
                PUSH + 0,                       // push 0 (the result)
                endLabel + ":"                  // end label
        );
    }

    /**
     * Generate code for the TimesNode node.
     *
     * @param node the TimesNode node
     * @return the code generated for the TimesNode node
     */
    @Override
    public String visitNode(final TimesNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),   // generate code for the left expression
                visit(node.right),  // generate code for the right expression
                MULT                // multiply the two expressions
        );
    }

    /**
     * Generate code for the DivNode node.
     *
     * @param node the DivNode node
     * @return the code generated for the DivNode node
     */
    @Override
    public String visitNode(final DivNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),   // generate code for left expression
                visit(node.right),  // generate code for right expression
                DIV                 // divide left value by right value
        );
    }

    /**
     * Generate code for the PlusNode node.
     *
     * @param node the PlusNode node
     * @return the code generated for the PlusNode node
     */
    @Override
    public String visitNode(final PlusNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),   // generate code for the left expression
                visit(node.right),  // generate code for the right expression
                ADD                 // add the two expressions
        );
    }

    /**
     * Generate code for the MinusNode node.
     *
     * @param node the MinusNode node
     * @return the code generated for the MinusNode node
     */
    @Override
    public String visitNode(final MinusNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.left),   // generate code for left expression
                visit(node.right),  // generate code for right expression
                SUB                 // subtract right value from left value
        );
    }

    /* *******************
     *********************
     * Values Nodes
     *********************
     ******************* */

    /**
     * Generate code for the BoolNode node.
     *
     * @param node the BoolNode node
     * @return the code generated for the BoolNode node
     */
    @Override
    public String visitNode(final BoolNode node) {
        if (print) printNode(node, String.valueOf(node.value));
        return PUSH + (node.value ? 1 : 0); // push 1 if true, 0 if false
    }

    /**
     * Generate code for the IntNode node.
     *
     * @param node the IntNode node
     * @return the code generated for the IntNode node
     */
    @Override
    public String visitNode(final IntNode node) {
        if (print) printNode(node, node.value.toString());
        return PUSH + node.value; // push the value of the integer
    }

    /**
     * Generate code for the IdNode node.
     *
     * @param node the IdNode node
     * @return the code generated for the IdNode node
     */
    @Override
    public String visitNode(final IdNode node) {
        if (print) printNode(node, node.id);
        String getARCode = null;
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) {
            getARCode = nlJoin(getARCode, LOAD_WORD);
        }
        return nlJoin(
                // Retrieve the AR where the variable is declared
                LOAD_FP, getARCode, /* retrieve address of frame containing "id" declaration,
                                           by following the static chain (of Access Links) */

                // Load the value of the variable
                PUSH + node.entry.offset,   // push offset of the variable
                ADD,                        // compute address of the variable
                LOAD_WORD                   // push value of the variable
        );
    }

    /* *******************
     *********************
     * Operations Nodes
     *********************
     ******************* */

    /**
     * Generate code for the PrintNode node.
     *
     * @param node the PrintNode node
     * @return the code generated for the PrintNode node
     */
    @Override
    public String visitNode(final PrintNode node) {
        if (print) printNode(node);
        return nlJoin(
                visit(node.exp),    // generate code for the expression
                PRINT               // print instruction
        );
    }

    /**
     * Generate code for the CallNode node.
     *
     * @param node the CallNode node
     * @return the code generated for the CallNode node
     */
    @Override
    public String visitNode(final CallNode node) {
        if (print) printNode(node, node.id);

        // Reverse argument list
        final List<Node> reversedArgumentsCode = new ArrayList<>(node.arguments);
        Collections.reverse(reversedArgumentsCode);

        String argumentsCode = null;
        for (final Node argument : reversedArgumentsCode) {
            argumentsCode = nlJoin(argumentsCode, visit(argument));
        }

        String getARCode = null;
        for (int i = 0; i < node.nestingLevel - node.entry.nl; i++) {
            getARCode = nlJoin(getARCode, LOAD_WORD);
        }

        return nlJoin(
                // Set up the stack frame
                LOAD_FP,     // push Control Link (pointer to frame of function "id" caller) on the stack
                argumentsCode,      // generate code for argument expressions in reversed order

                // Retrieve the AR where the function is declared
                LOAD_FP, getARCode, /* retrieve address of frame containing "id" declaration,
                                           by following the static chain (of Access Links) */

                STORE_TM,           // set $tm to popped value (the AR where the function is declared)
                LOAD_TM,            /* push Access Link (pointer to frame of function "id" declaration),
                                           it's for the AR of the function */

                LOAD_TM,                    // duplicate top of stack

                // load address of dispatch table if method
                (node.entry.type instanceof MethodTypeNode) ? LOAD_WORD : "",

                // load address of function
                PUSH + node.entry.offset,   // push offset of "id" declaration
                ADD,                        // compute address of "id" declaration
                LOAD_WORD,                  // push address of "id" function (the label of the function)

                // Jump to the function
                JUMP_SUBROUTINE  // jump to popped address (saving address of subsequent instruction in $ra)

        );
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
     * Generate code for the ClassNode node.
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

                    // Store method label in heap
                    PUSH + label,       // push method label
                    LOAD_HEAP_POINTER,  // push heap pointer
                    STORE_WORD,         // store method label in heap

                    // Increment heap pointer
                    LOAD_HEAP_POINTER,  // push heap pointer
                    PUSH + 1,           // push 1
                    ADD,                // heap pointer + 1
                    STORE_HP            // store heap pointer

            );
        }

        return nlJoin(
                LOAD_HEAP_POINTER,      // push heap pointer, the address of the dispatch table
                dispatchTableHeapCode   // generated code for creating the dispatch table in the heap
        );

    }

    /**
     * Generate code for the MethodNode node.
     *
     * @param node the MethodNode node
     * @return the code generated for the MethodNode node
     */
    @Override
    public String visitNode(final MethodNode node) {
        if (print) printNode(node);

        String declarationsCode = "";
        for (final DecNode declaration : node.declarations) {
            declarationsCode = nlJoin(
                    declarationsCode,
                    visit(declaration)
            );
        }

        String popDeclarationsCode = "";
        for (final DecNode declaration : node.declarations) {
            popDeclarationsCode = nlJoin(
                    popDeclarationsCode,
                    POP
            );
        }

        String popParametersCode = "";
        for (final ParNode parameter : node.params) {
            popParametersCode = nlJoin(
                    popParametersCode,
                    POP
            );
        }

        final String methodLabel = freshFunLabel();

        node.label = methodLabel; // set the label of the method

        // Generate code for the method body
        putCode(
                nlJoin(
                        methodLabel + ":",   // method label

                        // Set up the stack frame with FP, RA, and declarations
                        COPY_FP,                    // copy $sp to $fp, the new frame pointer
                        LOAD_RA,                    // push return address
                        declarationsCode,           // generate code for declarations

                        // Generate code for the body and store the result in $tm
                        visit(node.exp),            // generate code for the expression
                        STORE_TM,                   // set $tm to popped value (function result)

                        // Frame cleanup
                        popDeclarationsCode,        // pop declarations
                        STORE_RA,                   // pop return address to $ra (for return)
                        POP,                        // pop $fp
                        popParametersCode,          // pop parameters
                        STORE_FP,                   // pop $fp (restore old frame pointer)

                        // Return
                        LOAD_TM,                    // push function result
                        LOAD_RA,                    // push return address
                        JUMP_SUBROUTINE                  // jump to return address
                )
        );

        return null;
    }


    /* *******************
     *********************
     * Value Nodes
     *********************
     ******************* */

    /**
     * Generate code for the EmptyNode node.
     *
     * @param node the EmptyNode node
     * @return the code generated for the EmptyNode node
     */
    @Override
    public String visitNode(final EmptyNode node) {
        if (print) printNode(node);
        return PUSH + "-1";
    }

    /* *******************
     *********************
     * Operations Nodes
     *********************
     ******************* */

    /**
     * Generate code for the ClassCallNode node.
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

                // Set up the stack frame
                LOAD_FP,     // push $fp on the stack
                argumentsCode,      // generate arguments

                // Get the address of the object
                LOAD_FP, getARCode,         // get AR
                PUSH + node.entry.offset,   // push class offset on the stack
                ADD,                        // add class offset to $ar
                LOAD_WORD,                  // load object address


                // Duplicate class address
                STORE_TM,     // set $tm to popped value (class address)
                LOAD_TM,      // push class address on the stack
                LOAD_TM,      // duplicate class address

                // Get the address of the method
                LOAD_WORD,    // load dispatch table address
                PUSH + node.methodEntry.offset, // push method offset on the stack
                ADD,          // add method offset to dispatch table address
                LOAD_WORD,    // load method address

                // Call the method
                JUMP_SUBROUTINE
        );

    }

    /**
     * Generate code for the NewNode node.
     *
     * @param node the NewNode node
     * @return the code generated for the NewNode node
     */
    @Override
    public String visitNode(final NewNode node) {
        if (print) printNode(node);

        String argumentsCode = "";
        for (final Node argument : node.args) {
            argumentsCode = nlJoin(
                    argumentsCode,
                    visit(argument)
            );
        }

        String moveArgumentsOnHeapCode = "";
        for (final Node argument : node.args) {
            moveArgumentsOnHeapCode = nlJoin(
                    moveArgumentsOnHeapCode,

                    // Store argument on the heap
                    LOAD_HEAP_POINTER,    // push $hp on the stack
                    STORE_WORD,           // store argument on the heap

                    // Update $hp = $hp + 1
                    LOAD_HEAP_POINTER,    // push $hp on the stack
                    PUSH + 1,             // push 1 on the stack
                    ADD,                  // add 1 to $hp
                    STORE_HP              // store $hp
            );
        }

        return nlJoin(

                // Set up arguments on the stack and move them on the heap
                argumentsCode,      // generate arguments
                moveArgumentsOnHeapCode,  // move arguments on the heap

                // Load the address of the dispatch table in the heap
                PUSH + (ExecuteVM.MEMSIZE + node.entry.offset), // push class address on the stack
                LOAD_WORD,          // load dispatch table address
                LOAD_HEAP_POINTER,  // push $hp on the stack
                STORE_WORD,         // store dispatch table address on the heap

                // Put the result on the stack (object address)
                LOAD_HEAP_POINTER,  // push $hp on the stack (object address)

                // Update $hp = $hp + 1
                LOAD_HEAP_POINTER,  // push $hp on the stack
                PUSH + 1,           // push 1 on the stack
                ADD,                // add 1 to $hp
                STORE_HP            // store $hp
        );

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
        static final String BRANCH_LESS_EQUAL = "bleq "; // space needed for the argument

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
        static final String LOAD_HEAP_POINTER = "lhp";

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
        static final String JUMP_SUBROUTINE = "js";


    }

}