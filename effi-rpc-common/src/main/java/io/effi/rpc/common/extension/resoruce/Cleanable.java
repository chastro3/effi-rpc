package io.effi.rpc.common.extension.resoruce;

/**
 * Defines an object that can be cleared to reset its internal state or resources.
 */
public interface Cleanable {

    /**
     * Clears the object's internal state or resources, preparing it for reuse.
     */
    void clear();
}


