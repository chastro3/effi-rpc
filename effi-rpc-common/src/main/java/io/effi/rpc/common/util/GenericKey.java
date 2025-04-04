package io.effi.rpc.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a key for storing and retrieving type-safe attributes in a context.
 * Keys are unique and associated with specific types and names.
 *
 * @param <T> the type of the attribute value associated with this key
 */
public final class GenericKey<T> {

    // A thread-safe map for storing unique keys.
    private static final Map<String, GenericKey<?>> KEY_POOL = new ConcurrentHashMap<>();

    // The name of the attribute key.
    private final String name;

    // Private constructor to prevent direct instantiation.
    private GenericKey(String name) {
        this.name = name;
    }

    /**
     * Retrieves or creates a {@link GenericKey} instance for the given name.
     * Ensures a single instance per name for efficient key management.
     *
     * @param name the name of the attribute key
     * @param <T>  the type of the attribute value associated with this key
     * @return a {@link GenericKey} for the specified name
     */
    @SuppressWarnings("unchecked")
    public static <T> GenericKey<T> valueOf(String name) {
        return (GenericKey<T>) KEY_POOL.computeIfAbsent(name, k -> new GenericKey<T>(name));
    }

    /**
     * Returns the name of this attribute key.
     *
     * @return the name of the attribute key
     */
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

