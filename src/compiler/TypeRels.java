package compiler;

import compiler.AST.BoolTypeNode;
import compiler.AST.IntTypeNode;
import compiler.lib.TypeNode;
import java.util.HashMap;
import java.util.Map;

public class TypeRels {
    public static Map<String, String> superType = new HashMap<>();

    // valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
    public static boolean isSubtype(TypeNode a, TypeNode b) {
        return isEqual(a, b)
                || isBoolAndInt(a, b);
    }

    private static boolean isEqual(TypeNode a, TypeNode b) {
        return a.getClass().equals(b.getClass());
    }

    private static boolean isBoolAndInt(TypeNode a, TypeNode b) {
        return ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode));
    }

}
