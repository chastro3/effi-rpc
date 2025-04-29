package io.effi.rpc.util;

import io.effi.rpc.util.resoruce.Cleanable;

import java.util.function.Supplier;

/**
 * Manages a collection of attributes, allowing storage, retrieval, and removal.
 */
public interface Attributes extends Cleanable {

    /**
     * Gets the value associated with the key.
     */
    <T> T get(GenericKey<T> key);

    /**
     * Gets the value associated with the key, or returns a default if absent.
     */
    <T> T getOrDefault(GenericKey<T> key, T defaultValue);

    /**
     * Computes and stores a value if absent.
     */
    <T> T computeIfAbsent(GenericKey<T> key, Supplier<T> creator);

    /**
     * Sets the value for the key.
     */
    <T> T set(GenericKey<T> key, T value);

    /**
     * Removes the attribute for the key.
     */
    Attributes remove(GenericKey<?> key);
}



