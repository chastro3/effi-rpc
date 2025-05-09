package io.effi.rpc.contract;

import io.effi.rpc.config.NodeConfigSource;

import java.util.Collection;

/**
 * Manages a collection of {@link Invoker} instances indexed by key.
 */
public interface InvokerContainer<I extends Invoker<?>> extends NodeConfigSource {

    /**
     * Adds invoker by specified key.
     *
     * @param key     the key associated with the invoker
     * @param invoker the invoker to add
     */
    void addInvoker(String key, I invoker);

    /**
     * Retrieves invokers by key.
     *
     * @param key the key of the invoker
     * @return the invoker associated with the key, or {@code null} if not found
     */
    I getInvoker(String key);

    /**
     * Retrieves all invokers in the container.
     */
    Collection<I> invokers();

    /**
     * Generates unique keys for invokers based on protocol and path.
     *
     * @param protocol the protocol
     * @param path     the path
     * @return the generated key
     */
    static String generateInvokerKey(String protocol, String path) {
        return protocol + ":" + path;
    }
}



