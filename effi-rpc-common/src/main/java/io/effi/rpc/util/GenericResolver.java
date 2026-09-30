package io.effi.rpc.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves generic type arguments from class hierarchies.
 */
public final class GenericResolver {

    /**
     * Resolves a generic type argument from the target class hierarchy.
     *
     * @param targetClass concrete target class
     * @param baseClass generic base class or interface
     * @param index type argument index
     * @return resolved generic type
     * @throws IllegalArgumentException if the base type cannot be resolved
     */
    public static Type resolveGeneric(Class<?> targetClass, Class<?> baseClass, int index) {
        Map<TypeVariable<?>, Type> typeMap = new HashMap<>();
        buildTypeVariableMap(targetClass, typeMap);

        // Find the generic declaration of baseClass
        ParameterizedType basePT = findParameterizedType(targetClass, baseClass);
        if (basePT == null)
            throw new IllegalArgumentException("Cannot resolve generic for " + baseClass);

        Type t = basePT.getActualTypeArguments()[index];
        return resolveType(t, typeMap);
    }

    // Build the type-variable mapping across the full class hierarchy.
    private static void buildTypeVariableMap(Class<?> clazz,
                                             Map<TypeVariable<?>, Type> map) {

        if (clazz == null || clazz == Object.class) return;

        // Map type variables from the generic superclass first.
        Type superType = clazz.getGenericSuperclass();
        if (superType instanceof ParameterizedType pt) {
            putTypeArguments(pt, map);
            buildTypeVariableMap(clazz.getSuperclass(), map);
        }

        // Interface declarations can introduce independent type-variable mappings.
        for (Type t : clazz.getGenericInterfaces()) {
            if (t instanceof ParameterizedType pt) {
                putTypeArguments(pt, map);
                buildTypeVariableMap((Class<?>) pt.getRawType(), map);
            }
        }
    }

    private static void putTypeArguments(ParameterizedType pt, Map<TypeVariable<?>, Type> map) {
        Type[] actual = pt.getActualTypeArguments();
        TypeVariable<?>[] vars = ((Class<?>) pt.getRawType()).getTypeParameters();
        for (int i = 0; i < vars.length; i++) {
            map.put(vars[i], actual[i]);
        }
    }

    // Locate the parameterized declaration for the requested base type.
    private static ParameterizedType findParameterizedType(Class<?> clazz, Class<?> baseClass) {
        if (clazz == null || clazz == Object.class) return null;

        // Interfaces can hide the base type before the superclass chain is visited.
        for (Type t : clazz.getGenericInterfaces()) {
            if (t instanceof ParameterizedType pt
                    && pt.getRawType() == baseClass)
                return pt;

            if (t instanceof ParameterizedType pt2) {
                Type sub = findParameterizedType((Class<?>) pt2.getRawType(), baseClass);
                if (sub != null) return (ParameterizedType) sub;
            }
        }

        // Fall back to the superclass chain after interfaces.
        Type superType = clazz.getGenericSuperclass();
        if (superType instanceof ParameterizedType pt
                && pt.getRawType() == baseClass)
            return pt;

        return findParameterizedType(clazz.getSuperclass(), baseClass);
    }

    // Resolve nested type arguments recursively.
    private static Type resolveType(Type type, Map<TypeVariable<?>, Type> map) {
        if (type instanceof TypeVariable<?> tv) {
            return map.getOrDefault(tv, tv);
        }
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            Type[] resolved = new Type[args.length];
            for (int i = 0; i < args.length; i++) {
                resolved[i] = resolveType(args[i], map);
            }
            return new ResolvedParameterizedType(
                    (Class<?>) pt.getRawType(), resolved);
        }
        return type;
    }

    // Avoid external type libraries by exposing a minimal immutable parameterized type.
    private record ResolvedParameterizedType(Class<?> raw, Type[] args) implements ParameterizedType {

        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
