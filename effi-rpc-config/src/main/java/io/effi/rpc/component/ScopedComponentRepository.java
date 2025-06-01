package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.lifecycle.Lifecycle;
import io.effi.rpc.util.resoruce.Cleanable;

import java.util.Collection;
import java.util.Map;

/**
 * Manages scoped components, supporting singleton or multi-instance management.
 */
public interface ScopedComponentRepository extends Lifecycle, Cleanable {

    /**
     * Returns the name of this repository.
     */
    String name();

    /**
     * Returns the scope of this repository.
     */
    ScopedComponent.Scope scope();

    /**
     * Returns the parent repository, or {@code null} if none exists.
     */
    ScopedComponentRepository parent();

    /**
     * Registers a singleton component ({@link ScopedComponent.Kind#SINGLE}).
     */
    <T> ScopedComponentRepository register(Class<T> type, T component, String... tags);

    /**
     * Registers a keyed component ({@link ScopedComponent.Kind#MULTI}).
     */
    <T> ScopedComponentRepository register(Class<T> type, String key, T component, String... tags);

    /**
     * Looks up a singleton component ({@link ScopedComponent.Kind#SINGLE}).
     */
    <T> T lookup(Class<T> type);

    /**
     * Looks up a keyed component ({@link ScopedComponent.Kind#MULTI}).
     */
    <T> T lookup(Class<T> type, String key);

    /**
     * Returns the number of components of the given type.
     */
    int sizeOf(Class<?> type);

    /**
     * Lists all components of the given type.
     */
    <T> Collection<T> listOf(Class<T> type, String... tags);

    /**
     * Returns all components of the given type, mapped by key.
     */
    <T> Map<String, T> mapOf(Class<T> type, String... tags);

    /**
     * Removes the singleton component.
     */
    ScopedComponentRepository remove(Class<?> type);

    /**
     * Removes the keyed component.
     */
    ScopedComponentRepository remove(Class<?> type, String key);

}


