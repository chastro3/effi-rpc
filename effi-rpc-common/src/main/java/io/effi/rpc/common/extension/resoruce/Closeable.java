package io.effi.rpc.common.extension.resoruce;

/**
 * Represents a resource that can be closed and checked for activity.
 */
public interface Closeable {

    /**
     * Closes the resource, releasing any underlying resources.
     * Calling this method multiple times should have no adverse effect.
     */
    void close();

    /**
     * Checks if the resource is still active and usable.
     *
     * @return true if the resource is active, false if it is closed
     */
    boolean isActive();
}

