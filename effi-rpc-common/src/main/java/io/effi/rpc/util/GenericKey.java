package io.effi.rpc.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a type-safe key used for storing and retrieving attributes.
 *
 * @param <T> the type of value associated with this key
 */
public final class GenericKey<T> {

    private static final Map<String, GenericKey<?>> KEY_POOL = new ConcurrentHashMap<>();

    private final String name;

    private GenericKey(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public static <T> GenericKey<T> valueOf(String name) {
        return (GenericKey<T>) KEY_POOL.computeIfAbsent(name, k -> new GenericKey<T>(name));
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}


