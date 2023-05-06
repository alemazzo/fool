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

        final Stream<String> superTypes = Stream.iterate(aRefTypeNode.typeId, Objects::nonNull, superType::get);
        return superTypes
                .map(RefTypeNode::new)
                .filter(typeOfSuperA -> isSubtype(b, typeOfSuperA))
                .findFirst()
                .orElse(null);
    }

    public static boolean isSubtype(TypeNode a, TypeNode b) {
        return isBoolAndInt(a, b)
                || isEmptyTypeAndRefType(a, b)
                || isSubclass(a, b)
                || isMethodOverride(a, b);
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
            if (!isSubtype(bRefTypeNode.parlist.get(i), aRefTypeNode.parlist.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSubclass(TypeNode a, TypeNode b) {

        if (!(a instanceof RefTypeNode aRefTypeNode) || !(b instanceof RefTypeNode bRefTypeNode)) {
            return false;
        }

        final String aType = aRefTypeNode.typeId;
        final String bType = bRefTypeNode.typeId;

        if (aType.equals(bType)) {
            return true;
        }

        final Stream<String> superTypes = Stream.iterate(aType, Objects::nonNull, superType::get);
        return superTypes.anyMatch(bType::equals);

    }

    private static boolean isEmptyTypeAndRefType(TypeNode a, TypeNode b) {
        return ((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode));
    }

    private static boolean isBoolAndInt(TypeNode a, TypeNode b) {
        return ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode | b instanceof BoolTypeNode))
                || ((a instanceof IntTypeNode) && (b instanceof IntTypeNode));
    }

    private static boolean isEqual(TypeNode a, TypeNode b) {
        return a.getClass().equals(b.getClass());
    }

}
