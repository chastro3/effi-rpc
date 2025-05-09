package io.effi.rpc.util;

/**
 * Supports creating a deep copy of an object.
 */
public interface Replicable<T> {

    /**
     * Creates a deep copy, duplicating all mutable state.
     */
    T replicate();
}


