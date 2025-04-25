package io.effi.rpc.contract.repository;

import io.effi.rpc.util.resoruce.Cleanable;
import io.effi.rpc.contract.module.ModuleSource;

import java.util.Collection;

/**
 * Manages components of type {@link T} indexed by keys.
 *
 * @param <T> the type of components
 */
public interface ComponentRepository<T> extends ModuleSource, Cleanable {

    /**
     * Registers a component with the given key.
     *
     * @param key       the key
     * @param component the component to register
     */
    void register(String key, T component);

    /**
     * Removes the component associated with the given key.
     *
     * @param key the key of the component to remove
     */
    void remove(String key);

    /**
     * Retrieves the component associated with the given key.
     *
     * @param key the key of the component
     * @return the component, or {@code null} if not found
     */
    T get(String key);

    /**
     * Returns all managed components.
     */
    Collection<T> components();

    /**
     * Registers a component that implements {@link Key} using its own repository key.
     *
     * @param component the component to register
     */
    default void register(T component) {
        if (component instanceof ComponentRepository.Key key) {
            register(key.repositoryKey(), component);
        }
    }

    /**
     * Provides a key for repository registration.
     */
    interface Key {
        /**
         * Returns the repository key.
         *
         * @return the key as a string
         */
        String repositoryKey();
    }
}



