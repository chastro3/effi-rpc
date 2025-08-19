package io.effi.rpc.component.extension;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Provides access to extension loaders and extension instances.
 * <p>
 * Supports retrieval of extensions by type, name, or key,
 * including filtering and batch operations on extension collections.
 */
public interface ExtensionAccessor {

    /**
     * Returns the {@link ExtensionLoader} for the specified type.
     *
     * @param type the extension type
     * @return the extension loader, never {@code null}
     */
    <T> ExtensionLoader<T> extensionLoader(Class<T> type);

    /**
     * Returns the named extension instance for the specified type.
     * <p>
     * If the extension with the given {@code name} exists, it will be returned.
     * Otherwise, the primary extension for the given {@code type} will be returned.
     *
     * @param type the extension type
     * @param name the preferred extension name
     * @return the matching extension instance, or the primary extension if not found
     */
    <T> T preferredExtension(Class<T> type, String name);

    /**
     * Returns the named extension instance for the specified type.
     *
     * @param type the extension type
     * @param name the extension name
     * @return the extension instance, or {@code null} if not found
     */
    <T> T namedExtension(Class<T> type, String name);

    /**
     * Returns the adaptive extension instance for the specified type,
     * using the given name resolver.
     *
     * @param type       the extension type
     * @param nameGetter the function to resolve the extension name
     * @return the adaptive extension instance, or {@code null} if unavailable
     */
    <T> T adaptiveExtension(Class<T> type, Function<String, String> nameGetter);

    /**
     * Returns the primary extension instance for the specified type.
     *
     * @param type the extension type
     * @return the primary extension instance, or {@code null} if none
     */
    <T> T primaryExtension(Class<T> type);

    /**
     * Returns all available extension instances for the specified type.
     *
     * @param type the extension type
     * @return the collection of extensions, never {@code null}
     */
    default <T> Collection<T> extensions(Class<T> type) {
        return extensions(type, null);
    }

    /**
     * Returns all available extension instances for the specified type filtered by the given predicate.
     *
     * @param type   the extension type
     * @param filter the predicate to filter by name and holder; {@code null} disables filtering
     * @return the filtered collection of extensions, never {@code null}
     */
    <T> Collection<T> extensions(Class<T> type, BiPredicate<String, ExtensionEntry<T>> filter);

    /**
     * Returns a map of all available named extension instances for the specified type.
     *
     * @param type the extension type
     * @return the map of name to extension instance, never {@code null}
     */
    default <T> Map<String, T> namedExtensions(Class<T> type) {
        return namedExtensions(type, null);
    }

    /**
     * Returns a map of named extension instances for the specified type filtered by the given predicate.
     *
     * @param type   the extension type
     * @param filter the predicate to filter by name and holder; {@code null} disables filtering
     * @return the filtered map of name to extension instance, never {@code null}
     */
    <T> Map<String, T> namedExtensions(Class<T> type, BiPredicate<String, ExtensionEntry<T>> filter);

}



