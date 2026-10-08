package io.effi.rpc.util;

import io.effi.rpc.trait.Cleanable;

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
    <T> T computeIfAbsent(GenericKey<T> key, java.util.function.Supplier<T> creator);

    /**
     * Sets the value for the key.
     */
    <T> T set(GenericKey<T> key, T value);

    /**
     * Removes the attribute for the key.
     */
    Attributes remove(GenericKey<?> key);

    interface Supplier extends Attributes {

        @Override
        default <T> T get(GenericKey<T> key) {
            return attributes().get(key);
        }

        @Override
        default <T> T getOrDefault(GenericKey<T> key, T defaultValue) {
            return attributes().getOrDefault(key, defaultValue);
        }

        @Override
        default <T> T computeIfAbsent(GenericKey<T> key, java.util.function.Supplier<T> creator) {
            return attributes().computeIfAbsent(key, creator);
        }

        @Override
        default <T> T set(GenericKey<T> key, T value) {
            return attributes().set(key, value);
        }

        @Override
        default Attributes remove(GenericKey<?> key) {
            attributes().remove(key);
            return this;
        }

        Attributes attributes();

        @Override
        default void clear() {
            attributes().clear();
        }

    }
}



