package io.effi.rpc.component;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Provides access to extension loaders and extension instances.
 */
public interface ExtensionAccessor {

    /**
     * Returns the {@link ExtensionLoader} for the given type.
     */
    <T> ExtensionLoader<T> getLoader(Class<T> type);

    /**
     * Returns the named extension instance for the given type.
     */
    <T> T getExtension(Class<T> type, String name);

    /**
     * Returns the adaptive extension instance for the given type,using a custom name resolver.
     */
    <T> T getAdaptiveExtension(Class<T> type, Function<String, String> nameGetter);

    /**
     * Returns the default extension instance for the given type.
     */
    <T> T getDefaultExtension(Class<T> type);

    default <T> Collection<T> extensionsOf(Class<T> type) {
        return extensionsOf(type, null);
    }

    /**
     * Returns all available extensions for the given type.
     */
    <T> Collection<T> extensionsOf(Class<T> type, BiPredicate<String, ExtensionHolder<T>> filter);

    default <T> Map<String, T> extensionMapOf(Class<T> type) {
        return extensionMapOf(type, null);
    }

    /**
     * Returns all available extension for the given type.
     */
    <T> Map<String, T> extensionMapOf(Class<T> type, BiPredicate<String, ExtensionHolder<T>> filter);

}

