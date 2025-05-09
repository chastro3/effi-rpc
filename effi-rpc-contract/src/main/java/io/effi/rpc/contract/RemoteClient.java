package io.effi.rpc.contract;

/**
 * Wraps a client interface and manages its internal callers.
 */
public interface RemoteClient<T> extends InvokerContainer<Caller<?>> {

    /**
     * Returns the type of the client interface.
     */
    Class<T> targetType();

    /**
     * Gets the proxy instance.
     */
    T get();
}

