package io.effi.rpc.util;

/**
 * Builds an instance of type {@link T}.
 */
public interface Builder<T> {

    /**
     * Builds and returns an instance.
     */
    T build();
}

