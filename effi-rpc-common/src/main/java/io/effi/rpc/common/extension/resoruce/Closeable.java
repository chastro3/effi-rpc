package io.effi.rpc.common.extension.resoruce;

/**
 * Represents a resource that can be closed and checked for activity.
 */
public interface Closeable {

    /**
     * Closes the resource, releasing any underlying resources.
     */
    void close();

    /**
     * Checks if the resource is still active and usable.
     */
    boolean isActive();
}

