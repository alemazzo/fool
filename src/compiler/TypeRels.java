package compiler;

import compiler.AST.*;
import compiler.lib.TypeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class TypeRels {
    public static Map<String, String> superType = new HashMap<>();

    public static TypeNode lowestCommonAncestor(TypeNode a, TypeNode b) {
        if (isSubtype(a, b)) return b;
        if (isSubtype(b, a)) return a;

        if (!(a instanceof RefTypeNode aRefTypeNode)) return null;

        return superTypes(aRefTypeNode.typeId)
                .map(RefTypeNode::new)
                .filter(typeOfSuperA -> isSubtype(b, typeOfSuperA))
                .findFirst()
                .orElse(null);
    }

    private static Stream<String> superTypes(String type) {
        return Stream.iterate(type, Objects::nonNull, superType::get);
    }

    public static boolean isSubtype(TypeNode a, TypeNode b) {
        return isBoolAndInt(a, b)
                || isEmptyTypeAndRefType(a, b)
                || isSubclass(a, b)
                || isMethodOverride(a, b);
    }

    public static boolean isSupertype(TypeNode a, TypeNode b) {
        return isSubtype(b, a);
    }

    private static boolean isMethodOverride(TypeNode a, TypeNode b) {
        if (!(a instanceof ArrowTypeNode aRefTypeNode) || !(b instanceof ArrowTypeNode bRefTypeNode)) {
            return false;
        }

        // Covariance of return type
        if (!isSubtype(aRefTypeNode.ret, bRefTypeNode.ret)) {
            return false;
        }

        // Contravariance of parameters
        for (int i = 0; i < aRefTypeNode.parlist.size(); i++) {
            if (!isSupertype(aRefTypeNode.parlist.get(i), bRefTypeNode.parlist.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSubclass(TypeNode a, TypeNode b) {

        if (!(a instanceof RefTypeNode aRefTypeNode) || !(b instanceof RefTypeNode bRefTypeNode)) {
            return false;
        }

        return superTypes(aRefTypeNode.typeId)
                .anyMatch(bRefTypeNode.typeId::equals);

    }

    private static boolean isEmptyTypeAndRefType(TypeNode a, TypeNode b) {
        return ((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode));
    }

    private static boolean isBoolAndInt(TypeNode a, TypeNode b) {
        return ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode | b instanceof BoolTypeNode))
                || ((a instanceof IntTypeNode) && (b instanceof IntTypeNode));
    }
}
