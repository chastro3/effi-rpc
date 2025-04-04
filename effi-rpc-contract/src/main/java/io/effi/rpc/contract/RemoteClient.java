package io.effi.rpc.contract;

/**
 * Remote caller for invoking remote service methods.
 *
 * @param <T> The type of the remote service interface.
 */
public interface RemoteClient<T> extends InvokerContainer<Caller<?>> {

    /**
     * Returns the target interface of the remote service.
     *
     * @return the target interface
     */
    Class<T> targetType();

    /**
     * Gets a Proxy instance of the remote service.
     *
     * @return proxy instance
     */
    T get();
}

