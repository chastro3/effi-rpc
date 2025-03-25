package io.effi.rpc.common.extension;

import io.effi.rpc.common.extension.resoruce.Cleanable;

import java.util.function.Supplier;

/**
 * Manages a collection of attributes, allowing storage, retrieval, and removal.
 */
public interface Attributes extends Cleanable {

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the attribute key
     * @param <T> the type of the value
     * @return the associated value, or {@code null} if not present
     */
    <T> T get(GenericKey<T> key);

    /**
     * Retrieves the value associated with the given key, or returns a default value if absent.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return if the key is absent
     * @param <T>          the type of the value
     * @return the associated value, or {@code defaultValue} if not present
     */
    <T> T getOrDefault(GenericKey<T> key, T defaultValue);

    /**
     * Computes and stores a value if the key is absent; otherwise, returns the existing value.
     *
     * @param key     the attribute key
     * @param creator a supplier for the value if absent
     * @param <T>     the type of the value
     * @return the associated or computed value
     */
    <T> T computeIfAbsent(GenericKey<T> key, Supplier<T> creator);

    /**
     * Sets the value for the given key.
     *
     * @param key   the attribute key
     * @param value the value to store
     * @param <T>   the type of the value
     * @return the stored value
     */
    <T> T set(GenericKey<T> key, T value);

    /**
     * Removes the attribute for the given key.
     *
     * @param key the attribute key
     * @return this instance for method chaining
     */
    Attributes remove(GenericKey<?> key);
}


