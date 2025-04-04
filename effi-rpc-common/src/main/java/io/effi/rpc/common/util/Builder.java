package io.effi.rpc.common.util;

/**
 * A generic builder interface for constructing objects of type T.
 * Typically used in the Builder design pattern for step-by-step object construction.
 *
 * @param <T> the type of object this builder constructs
 */
public interface Builder<T> {

    /**
     * Builds and returns an instance.
     */
    T build();
}
