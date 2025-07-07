package io.effi.rpc.util.resoruce;

/**
 * Represents a resource that can be cleared.
 */
@FunctionalInterface
public interface Cleanable {

    /**
     * Clears the resource.
     */
    void clear();
}


