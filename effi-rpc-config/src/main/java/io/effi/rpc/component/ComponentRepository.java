package io.effi.rpc.component;

import io.effi.rpc.util.resoruce.Cleanable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Manages registrations and lookups of components by key.
 */
public interface ComponentRepository<K, T> extends Cleanable {

    /**
     * Registers a component with the given key.
     *
     * @param key       the component key
     * @param component the component to register
     * @return this repository instance
     */
    ComponentRepository<K, T> register(K key, T component);

    /**
     * Registers a component with the given key, computing the component if it does not exist.
     *
     * @param key      the component key
     * @param supplier the supplier to compute the component
     * @return this repository instance
     */
    T computeIfAbsent(K key, Function<K, T> supplier);

    /**
     * Removes the component associated with the given key.
     *
     * @param key the component key
     * @return this repository instance
     */
    ComponentRepository<K, T> remove(K key);

    /**
     * Looks up the component by key.
     *
     * @param key the component key
     * @return the matched component, or {@code null} if not found
     */
    T lookup(K key);

    /**
     * Returns the number of registered components.
     */
    int size();

    /**
     * Returns all registered components.
     */
    Collection<T> components();

    /**
     * Returns all registered components as a map.
     */
    Map<K, T> toMap();
}

