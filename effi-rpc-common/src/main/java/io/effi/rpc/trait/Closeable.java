package io.effi.rpc.trait;

/**
 * Represents resources that can be closed and monitored for activity.
 * <p>
 * Provides a standardized interface for resource lifecycle management
 * with support for closing resources and checking their active status.
 */
public interface Closeable {

    /**
     * Closes the resource, releasing any underlying resources.
     */
    void close();

    /**
     * Returns true if the current resource is active.
     */
    boolean active();

}

