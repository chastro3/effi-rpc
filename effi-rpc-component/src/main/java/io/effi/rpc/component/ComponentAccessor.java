package io.effi.rpc.component;

import io.effi.rpc.util.GenericKey;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Provides access to component loaders and component instances.
 * <p>
 * Supports retrieval of components by type, id, or key,
 * including filtering and batch operations on component collections.
 */
public interface ComponentAccessor {

    /**
     * Returns the singleton component of the specified type.
     *
     * @param type the component type
     * @return the component instance, or {@code null} if not found
     */
    <T> T singleComponent(Class<T> type);

    /**
     * Returns the named component of the specified type.
     *
     * @param type the component type
     * @param name the component id
     * @return the component instance, or {@code null} if not found
     */
    <T> T namedComponent(Class<T> type, String name);

    /**
     * Returns the external component identified by the given generic key.
     *
     * @param name the generic key of the component
     * @param <T>  the component type
     * @return the wrapped component instance, or {@code null} if not found
     */
    <T> T externalComponent(GenericKey<T> name);

    /**
     * Returns all components of the specified type, filtered by the given predicate.
     *
     * @param type   the component type
     * @param filter predicate to filter by component id and instance; {@code null} disables filtering
     * @return the collection of components, never {@code null}
     */
    <T> Collection<T> components(Class<T> type, BiPredicate<String, T> filter);

    /**
     * Returns a map of named components of the specified type, filtered by the given predicate.
     *
     * @param type   the component type
     * @param filter predicate to filter by component id and instance; {@code null} disables filtering
     * @return the map of component names to instances, never {@code null}
     */
    <T> Map<String, T> namedComponents(Class<T> type, BiPredicate<String, T> filter);

    /**
     * Returns all components of the specified type.
     *
     * @param type the component type
     * @return the collection of components, never {@code null}
     */
    default <T> Collection<T> components(Class<T> type) {
        return components(type, null);
    }

    /**
     * Returns a map of all named components of the specified type.
     *
     * @param type the component type
     * @return the map of component names to instances, never {@code null}
     */
    default <T> Map<String, T> namedComponents(Class<T> type) {
        return namedComponents(type, null);
    }

    /**
     * Returns the number of components registered for the specified type.
     *
     * @param type the component type
     */
    int componentCount(Class<?> type);

}
