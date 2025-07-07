package io.effi.rpc.component;

import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.resoruce.Cleanable;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Manages components with register, lookup, and remove operations.
 */
public interface ComponentStore extends Cleanable {

    static String acquireName(Object component) {
        String name = null;
        if (component instanceof Identifiable identifiable) {
            name = identifiable.id();
        }
        return StringUtil.isBlank(name)
                ? ObjectUtil.lowercaseName(component.getClass())
                : name;
    }

    /**
     * Registers a singleton component or a component that implements {@link Identifiable}.
     */
    <T> ComponentStore register(Class<T> type, T component);

    /**
     * Registers a named component.
     */
    <T> ComponentStore register(Class<T> type, String name, T component);

    /**
     * Looks up a singleton component.
     */
    <T> T lookup(Class<T> type);

    /**
     * Looks up a named component.
     */
    <T> T lookup(Class<T> type, String name);

    /**
     * Looks up a wrapped component.
     */
    <T> T lookupWrapped(GenericKey<T> name);

    /**
     * Returns the number of components of the given type.
     */
    int sizeOf(Class<?> type);

    /**
     * Removes the singleton component.
     */
    ComponentStore remove(Class<?> type);

    /**
     * Removes the named component.
     */
    ComponentStore remove(Class<?> type, String name);

    default <T> Collection<T> listOf(Class<T> type) {
        return listOf(type, null);
    }

    /**
     * Lists all components of the given type.
     */
    <T> Collection<T> listOf(Class<T> type, BiPredicate<String, T> filter);

    default <T> Map<String, T> mapOf(Class<T> type) {
        return mapOf(type, null);
    }

    /**
     * Returns all components of the given type, mapped by name.
     */
    <T> Map<String, T> mapOf(Class<T> type, BiPredicate<String, T> filter);

}
