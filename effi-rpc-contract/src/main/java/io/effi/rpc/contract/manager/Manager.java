package io.effi.rpc.contract.manager;

import io.effi.rpc.common.extension.resoruce.Cleanable;
import io.effi.rpc.contract.module.ModuleSource;

import java.util.Collection;

/**
 * Manages a collection of values of type {@link T}, indexed by a specified key.
 *
 * @param <T> the type of values managed
 */
public interface Manager<T> extends ModuleSource, Cleanable {

    /**
     * Registers a value with a specific key.
     *
     * @param key   the key for the value
     * @param value the value to register
     */
    void register(String key, T value);

    /**
     * Removes the value associated with the specified key.
     *
     * @param key the key whose associated value should be removed
     */
    void remove(String key);

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key whose associated value is to be retrieved
     * @return the value, or {@code null} if no value is associated
     */
    T get(String key);

    /**
     * Returns all values managed by this {@link Manager}.
     *
     * @return a collection of values
     */
    Collection<T> values();

    /**
     * Registers a value if it implements the {@link Key} interface.
     * The key is derived from the value itself for implicit registration.
     *
     * @param value the value to register
     */
    default void register(T value) {
        if (value instanceof Manager.Key managerValue) {
            register(managerValue.managerKey(), value);
        }
    }

    /**
     * A key that can be associated with a value in the {@link Manager}.
     * Implementations should provide the key as a string.
     */
    interface Key {
        /**
         * Returns the key associated with this instance.
         *
         * @return the key
         */
        String managerKey();
    }
}


