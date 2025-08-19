package io.effi.rpc.util.resoruce;

/**
 * Represents resources that can be cleared or cleaned up.
 * <p>
 * Provides a standardized interface for resource cleanup operations,
 * enabling consistent resource management across different components.
 */
@FunctionalInterface
public interface Cleanable {

    /**
     * Clears the resource.
     */
    void clear();
}


