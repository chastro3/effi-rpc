package io.effi.rpc.nativetools;

/**
 * Represents a native configuration item.
 *
 * @param <T> the type of the JSON config.
 */
public interface NativeConfig<T> {

    /**
     * Gets the name of the native configuration item.
     */
    String name();

    /**
     * Converts this native configuration item to its JSON representation.
     */
    T toJsonConfig();

    /**
     * Checks if the configuration item has a resource.
     */
    boolean hasResource();
}

