package io.effi.rpc.util;

/**
 * Builds an instance of type {@link T}.
 *
 * @param <T> the type of object this builder creates
 */
public interface Builder<T> {

    /**
     * Builds and returns an instance.
     */
    T build();
}

