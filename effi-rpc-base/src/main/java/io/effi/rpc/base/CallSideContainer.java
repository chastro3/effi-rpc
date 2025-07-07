package io.effi.rpc.base;

import io.effi.rpc.config.NodeConfig;

import java.util.Collection;

/**
 * Manages a collection of {@link CallSide} instance indexed by key.
 */
public interface CallSideContainer<I extends CallSide> extends NodeConfig.Provider {

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
    static String invokerKey(String protocol, String path) {
        return "(" + protocol + ")" + path;
    }
}



