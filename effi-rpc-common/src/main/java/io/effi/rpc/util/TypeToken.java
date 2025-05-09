package io.effi.rpc.util;

import java.lang.reflect.*;

/**
 * Captures and represents generic type information.
 */
public abstract class TypeToken<T> {

    private final Type type;

    private final Class<? super T> rawType;

    /**
     * Constructs a TypeToken that captures the generic type of the
     * subclass. This is achieved by using an anonymous class.
     */
    protected TypeToken() {
        this.type = getSuperclassTypeParameter();
        this.rawType = getRawType(this.type);
    }

    protected TypeToken(Type type) {
        this.type = type;
        this.rawType = getRawType(type);
    }

    public static <T> TypeToken<T> get(Type type) {
        return new TypeToken<>(type) {};
    }

    public Type type() {
        return type;
    }

    public Class<? super T> rawType() {
        return rawType;
    }

    @SuppressWarnings("unchecked")
    private Class<? super T> getRawType(Type type) {
        // Determines the raw type of the provided Type instance
        switch (type) {
            case Class<?> ignored -> {
                // The type is a normal class
                return (Class<? super T>) type;
            }
            case ParameterizedType parameterizedType -> {
                // Handles ParameterizedType to extract raw type
                Type rawType = parameterizedType.getRawType();
                if (!(rawType instanceof Class)) {
                    throw new IllegalArgumentException();
                }
                return (Class<? super T>) rawType;
            }
            case GenericArrayType genericArrayType -> {
                // Handles array types by creating a new array instance
                Type componentType = genericArrayType.getGenericComponentType();
                return (Class<? super T>) Array.newInstance(getRawType(componentType), 0).getClass();
            }
            case TypeVariable<?> ignored -> {
                // Wildcards and type variables return Object as a fallback
                return Object.class;
            }
            case WildcardType wildcardType -> {
                // Uses the first upper bound of the wildcard
                Type[] bounds = wildcardType.getUpperBounds();
                assert bounds.length == 1; // Ensure there is only one bound
                return getRawType(bounds[0]);
            }
            case null, default -> {
                // Throws an exception if type is not recognized
                String className = type == null ? "null" : type.getClass().getName();
                throw new IllegalArgumentException(String.format("Expected a Class, ParameterizedType, or GenericArrayType, but <%s> is of type %s", type, className));
            }
        }
    }

    private Type getSuperclassTypeParameter() {
        // Returns the generic type information of the immediate parent class
        Type superclass = getClass().getGenericSuperclass();
        // Validates that the parent class is a ParameterizedType
        if (superclass instanceof ParameterizedType) {
            return ((ParameterizedType) superclass).getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("Invalid TypeToken construction.");
        }
    }
}

