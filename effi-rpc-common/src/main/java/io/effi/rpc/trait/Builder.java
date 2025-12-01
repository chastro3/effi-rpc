package io.effi.rpc.trait;

/**
 * Builds instances of specified types.
 * <p>
 * Provides a standardized interface for constructing objects
 * through a build method that returns the configured instance.
 */
public interface Builder<T> {

    /**
     * Builds and returns an instance.
     */
    T build();
}

