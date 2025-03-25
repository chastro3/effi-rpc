package io.effi.rpc.contract;

import io.effi.rpc.common.url.ConfigSource;

import java.util.Collection;

/**
 * Container for managing invokers.
 *
 * @param <I> the type of invoker managed
 */
public interface InvokerContainer<I extends Invoker<?>> extends ConfigSource {

    /**
     * Adds an invoker to the container with a specified key.
     *
     * @param key     the key associated with the invoker
     * @param invoker the invoker to add
     */
    void addInvoker(String key, I invoker);

    /**
     * Retrieves the invoker associated with the specified key.
     *
     * @param key the key of the invoker
     * @return the invoker associated with the key, or {@code null} if not found
     */
    I getInvoker(String key);

    /**
     * Retrieves all invokers in the container.
     *
     * @return a collection of all invokers
     */
    Collection<I> invokers();
}


