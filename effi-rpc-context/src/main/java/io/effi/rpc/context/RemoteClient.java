package io.effi.rpc.context;

/**
 * Wraps a client interface and manages its internal callers.
 */
public interface RemoteClient<T> extends PeerContainer<Caller<?>> {

    /**
     * Returns the type of the client interface.
     */
    Class<T> targetType();

    /**
     * Gets the proxy instance.
     */
    T get();
}

