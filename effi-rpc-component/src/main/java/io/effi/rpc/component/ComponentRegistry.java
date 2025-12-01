package io.effi.rpc.component;

import io.effi.rpc.trait.Identifiable;
import io.effi.rpc.trait.Cleanable;

/**
 * Manages registration and removal of components within a registry.
 * <p>
 * Provides methods for registering singleton and named components,
 * with support for removal by type and id.
 */
public interface ComponentRegistry extends Cleanable {

    /**
     * Registers a singleton component or a named component implementing {@link Identifiable}.
     *
     * @param type      the component type
     * @param component the component instance
     */
    <T> ComponentRegistry register(Class<T> type, T component);

    /**
     * Registers a named component.
     *
     * @param type      the component type
     * @param name      the component id
     * @param component the component instance
     */
    <T> ComponentRegistry register(Class<T> type, String name, T component);

    /**
     * Removes the singleton component of the specified type.
     *
     * @param type the component type
     */
    ComponentRegistry remove(Class<?> type);

    /**
     * Removes the named component of the specified type.
     *
     * @param type the component type
     * @param name the component id
     */
    ComponentRegistry remove(Class<?> type, String name);

}
