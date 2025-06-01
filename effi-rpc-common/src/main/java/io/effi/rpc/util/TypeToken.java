package io.effi.rpc.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

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
        if (type instanceof Class<?>) {
            // The type is a normal class
            return (Class<? super T>) type;
        } else if (type instanceof ParameterizedType parameterizedType) {
            // Handles ParameterizedType to extract raw type
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalArgumentException();
            }
            return (Class<? super T>) rawType;
        } else if (type instanceof GenericArrayType genericArrayType) {
            // Handles array types by creating a new array instance
            Type componentType = genericArrayType.getGenericComponentType();
            return (Class<? super T>) Array.newInstance(getRawType(componentType), 0).getClass();
        } else if (type instanceof TypeVariable) {
            // Wildcards and type variables return Object as a fallback
            return Object.class;
        } else if (type instanceof WildcardType wildcardType) {
            // Uses the first upper bound of the wildcard
            Type[] bounds = wildcardType.getUpperBounds();
            assert bounds.length == 1; // Ensure there is only one bound
            return getRawType(bounds[0]);
        } else {
            // Throws an exception if type is not recognized
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException(StringUtil.format(
                    "Expected a Class, ParameterizedType, or GenericArrayType, but '{}' is of type '{}'",
                    type, className
            ));
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

