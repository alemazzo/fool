package compiler;

import compiler.AST.*;
import compiler.lib.TypeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class TypeRels {

    /**
     * Map of the super types of each type.
     * It is filled in the {@link TypeCheckEASTVisitor}.
     */
    public static Map<String, String> superType = new HashMap<>();

    /**
     * Compute the lowest common ancestor of two types.
     * It traverses the inheritance tree of the first type
     * and checks if the second type is a subtype of any of the
     * super types.
     * If it is, then the super type is the lowest common ancestor.
     * It's used to compute the type of the if-then-else expression in {@link TypeCheckEASTVisitor}.
     *
     * @param first  The first type
     * @param second The second type
     * @return The lowest common ancestor of the two types
     */
    public static TypeNode lowestCommonAncestor(final TypeNode first, final TypeNode second) {
        if (isSubtype(first, second)) return second;
        if (isSubtype(second, first)) return first;

        if (!(first instanceof RefTypeNode firstRefTypeNode)) return null;

        return superTypes(firstRefTypeNode.typeId)
                .map(RefTypeNode::new)
                .filter(typeOfSuperA -> isSubtype(second, typeOfSuperA))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the Stream of super types of a given type.
     * It starts from the given type and traverses the inheritance tree
     * until it reaches the top of the tree.
     *
     * @param type The type to start from
     * @return The Stream of super types
     */
    private static Stream<String> superTypes(final String type) {
        return Stream.iterate(type, Objects::nonNull, superType::get);
    }

    /**
     * Check if the first type is a subtype of the second type.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is a subtype of the second type, false otherwise
     */
    public static boolean isSubtype(final TypeNode first, final TypeNode second) {
        return isBoolAndInt(first, second)
                || isEmptyTypeAndRefType(first, second)
                || isSubclass(first, second)
                || isMethodOverride(first, second);
    }

    /**
     * Check if the first type is a supertype of the second type.
     * It is the same as {@link #isSubtype(TypeNode, TypeNode)} but with the types swapped.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is a supertype of the second type, false otherwise
     */
    public static boolean isSupertype(final TypeNode first, final TypeNode second) {
        return isSubtype(second, first);
    }

    /**
     * Check if both types are ArrowTypeNode and
     * if the first type is a subtype of the second type.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is a subtype of the second type, false otherwise
     */
    private static boolean isMethodOverride(final TypeNode first, final TypeNode second) {
        if (!(first instanceof ArrowTypeNode firstArrowTypeNode) ||
                !(second instanceof ArrowTypeNode secondArrowTypeNode)) {
            return false;
        }

        // Covariance of return type
        if (!isSubtype(firstArrowTypeNode.returnType, secondArrowTypeNode.returnType)) {
            return false;
        }

        // Contravariance of parameters
        for (int i = 0; i < firstArrowTypeNode.parameters.size(); i++) {
            if (!isSupertype(firstArrowTypeNode.parameters.get(i), secondArrowTypeNode.parameters.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if the first type is a subclass of the second type.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is a subclass of the second type, false otherwise
     */
    private static boolean isSubclass(final TypeNode first, final TypeNode second) {

        if (!(first instanceof RefTypeNode firstRefTypeNode)
                || !(second instanceof RefTypeNode secondRefTypeNode)) {
            return false;
        }

        return superTypes(firstRefTypeNode.typeId)
                .anyMatch(secondRefTypeNode.typeId::equals);

    }

    /**
     * Check if the first type is EmptyTypeNode and the second type is RefTypeNode.
     * This is needed to handle null values.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is EmptyTypeNode and the second type is RefTypeNode, false otherwise
     */
    private static boolean isEmptyTypeAndRefType(final TypeNode first, final TypeNode second) {
        return ((first instanceof EmptyTypeNode) && (second instanceof RefTypeNode));
    }

    /**
     * Check if the first type is BoolTypeNode and the second type is IntTypeNode or BoolTypeNode.
     * This is needed to handle the if-then-else statement.
     *
     * @param first  The first type
     * @param second The second type
     * @return True if the first type is BoolTypeNode and the second type is IntTypeNode or BoolTypeNode, false otherwise
     */
    private static boolean isBoolAndInt(final TypeNode first, final TypeNode second) {
        return ((first instanceof BoolTypeNode) && (second instanceof IntTypeNode | second instanceof BoolTypeNode))
                || ((first instanceof IntTypeNode) && (second instanceof IntTypeNode));
    }
}
