package io.effi.rpc.common.extension;

/**
 * Enables creating a deep copy of an object.
 *
 * @param <T> the type of the object
 */
public interface Replicable<T> {

    /**
     * Creates a deep copy of the object, duplicating all internal mutable state.
     */
    T replicate();
}

