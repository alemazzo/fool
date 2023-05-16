package svm;

public class ExecuteVM {

    public static final int CODESIZE = 10000;
    public static final int MEMSIZE = 10000;

    private final int[] code;
    private final int[] memory = new int[MEMSIZE];

    /* Il registro ip contiene l'indirizzo dell'istruzione da eseguire */
    private int ip = 0;

    /* Il registro sp contiene l'indirizzo della prima locazione di memoria
       non occupata nello stack */
    private int sp = MEMSIZE;

    /* Il registro hp contiene l'indirizzo della prima locazione di memoria
       non occupata nell'heap */
    private int hp = 0;

    /* Il registro fp contiene l'indirizzo del frame pointer */
    private int fp = MEMSIZE;

    /* Il registro ra contiene l'indirizzo di ritorno */
    private int ra;

    /* Il registro tm contiene un valore temporaneo */
    private int tm;

    public ExecuteVM(int[] code) {
        this.code = code;
    }

    public void cpu() {
        while (true) {
            int bytecode = code[ip++]; // fetch
            int v1, v2;
            int address;
            switch (bytecode) {
                case SVMParser.PUSH:
                    push(code[ip++]);
                    break;
                case SVMParser.POP:
                    pop();
                    break;
                case SVMParser.ADD:
                    v1 = pop();
                    v2 = pop();
                    push(v2 + v1);
                    break;
                case SVMParser.MULT:
                    v1 = pop();
                    v2 = pop();
                    push(v2 * v1);
                    break;
                case SVMParser.DIV:
                    v1 = pop();
                    v2 = pop();
                    push(v2 / v1);
                    break;
                case SVMParser.SUB:
                    v1 = pop();
                    v2 = pop();
                    push(v2 - v1);
                    break;
                case SVMParser.STOREW: //
                    address = pop();
                    memory[address] = pop();
                    break;
                case SVMParser.LOADW: //
                    push(memory[pop()]);
                    break;
                case SVMParser.BRANCH:
                    address = code[ip];
                    ip = address;
                    break;
                case SVMParser.BRANCHEQ:
                    address = code[ip++];
                    v1 = pop();
                    v2 = pop();
                    if (v2 == v1) ip = address;
                    break;
                case SVMParser.BRANCHLESSEQ:
                    address = code[ip++];
                    v1 = pop();
                    v2 = pop();
                    if (v2 <= v1) ip = address;
                    break;
                case SVMParser.JS: //
                    address = pop();
                    ra = ip;
                    ip = address;
                    break;
                case SVMParser.STORERA: //
                    ra = pop();
                    break;
                case SVMParser.LOADRA: //
                    push(ra);
                    break;
                case SVMParser.STORETM:
                    tm = pop();
                    break;
                case SVMParser.LOADTM:
                    push(tm);
                    break;
                case SVMParser.LOADFP: //
                    push(fp);
                    break;
                case SVMParser.STOREFP: //
                    fp = pop();
                    break;
                case SVMParser.COPYFP: //
                    fp = sp;
                    break;
                case SVMParser.STOREHP: //
                    hp = pop();
                    break;
                case SVMParser.LOADHP: //
                    push(hp);
                    break;
                case SVMParser.PRINT:
                    System.out.println((sp < MEMSIZE) ? memory[sp] : "Empty stack!");
                    break;
                case SVMParser.HALT:
                    return;
            }
        }
    }

    private int pop() {
        return memory[sp++];
    }

    private void push(int v) {
        memory[--sp] = v;
    }

}