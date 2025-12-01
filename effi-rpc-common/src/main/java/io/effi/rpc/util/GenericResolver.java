package io.effi.rpc.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public final class GenericResolver {

    /**
     * Resolve the actual generic type (supporting inheritance, interfaces, nested generics)
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

    // Build T -> RealType map for entire class hierarchy
    private static void buildTypeVariableMap(Class<?> clazz,
                                             Map<TypeVariable<?>, Type> map) {

        if (clazz == null || clazz == Object.class) return;

        // Process generic superclass
        Type superType = clazz.getGenericSuperclass();
        if (superType instanceof ParameterizedType pt) {
            putTypeArguments(pt, map);
            buildTypeVariableMap(clazz.getSuperclass(), map);
        }

        // Process generic interfaces
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

    // Find where baseClass is actually implemented in the hierarchy
    private static ParameterizedType findParameterizedType(Class<?> clazz, Class<?> baseClass) {
        if (clazz == null || clazz == Object.class) return null;

        // Check interfaces
        for (Type t : clazz.getGenericInterfaces()) {
            if (t instanceof ParameterizedType pt
                    && pt.getRawType() == baseClass)
                return pt;

            if (t instanceof ParameterizedType pt2) {
                Type sub = findParameterizedType((Class<?>) pt2.getRawType(), baseClass);
                if (sub != null) return (ParameterizedType) sub;
            }
        }

        // Check superclass
        Type superType = clazz.getGenericSuperclass();
        if (superType instanceof ParameterizedType pt
                && pt.getRawType() == baseClass)
            return pt;

        return findParameterizedType(clazz.getSuperclass(), baseClass);
    }

    // Resolve nested generic type recursively
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

    // Lightweight immutable parameterized type
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
