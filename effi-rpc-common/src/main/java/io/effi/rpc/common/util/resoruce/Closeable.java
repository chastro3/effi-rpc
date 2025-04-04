package io.effi.rpc.common.util.resoruce;

/**
 * Represents a resource that can be closed and checked for activity.
 */
public interface Closeable {

    /**
     * Closes the resource, releasing any underlying resources.
     */
    void close();

    /**
     * Returns the heartBeatHandler.
     */
    boolean isActive();
}

