package io.effi.rpc.component;

import io.effi.rpc.util.resoruce.Cleanable;

import java.util.Map;

/**
 * Manages bean registration, lookup, and removal within a scoped context.
 */
public interface BeanFactory extends ScopedContextOwned, Cleanable {

    /**
     * Checks if a bean of the given type and name exists.
     */
    boolean containsBean(Class<?> type, String name);

    /**
     * Registers a bean with the given type and name.
     */
    <T> BeanFactory registerBean(Class<T> type, String name, T bean);

    /**
     * Returns the singleton bean of the given type.
     */
    <T> T getBean(Class<T> type);

    /**
     * Returns the named bean of the given type.
     */
    <T> T getBean(Class<T> type, String name);

    /**
     * Returns all registered names for the given bean type.
     */
    String[] getBeanNames(Class<?> type);

    /**
     * Returns all beans of the given type mapped by name.
     */
    <T> Map<String, T> getBeans(Class<T> type);

    /**
     * Removes the singleton bean of the given type.
     */
    BeanFactory removeBean(Class<?> type);

    /**
     * Removes the named bean of the given type.
     */
    BeanFactory removeBean(Class<?> type, String name);
}

