package io.effi.rpc.common.util;

import java.lang.reflect.*;

/**
 * Capture and represent generic type information.
 *
 * @param <T> The type parameter.
 */
public abstract class TypeToken<T> {

    private final Type type; // The captured type

    private final Class<? super T> rawType; // The raw class type of T

    /**
     * Constructs a TypeToken that captures the generic type of the
     * subclass. This is achieved by using an anonymous class.
     */
    protected TypeToken() {
        this.type = getSuperclassTypeParameter();
        this.rawType = getRawType(this.type);
    }

    /**
     * Constructs a TypeToken with a specified type.
     *
     * @param type The type to be captured
     */
    protected TypeToken(Type type) {
        this.type = type;
        this.rawType = getRawType(type);
    }

    /**
     * Creates a TypeToken for the given type.
     *
     * @param type The type to be wrapped
     * @param <T>
     * @return A new instance of TypeToken capturing the specified type
     */
    public static <T> TypeToken<T> get(Type type) {
        return new TypeToken<>(type) {};
    }

    /**
     * Returns the captured type.
     *
     * @return The captured type
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the raw class type of the captured type.
     *
     * @return The raw class type
     */
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

