package io.effi.rpc.trait;

/**
 * Provides unique identification capabilities for objects.
 * <p>
 * Enables objects to have a unique identifier for tracking and referencing purposes.
 */
public interface Identifiable {

    /**
     * Returns the unique identifier of this instance.
     */
    String id();
}

