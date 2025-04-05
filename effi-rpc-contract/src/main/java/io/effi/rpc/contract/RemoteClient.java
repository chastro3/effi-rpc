package io.effi.rpc.contract;

/**
 * Remote client for invoking remote service methods.
 *
 * @param <T> The type of the remote service interface.
 */
public interface RemoteClient<T> extends InvokerContainer<Caller<?>> {

    /**
     * Returns the target interface of the remote service.
     */
    Class<T> targetType();

    /**
     * Gets the proxy instance of the remote service.
     */
    T get();
}

