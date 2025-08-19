package io.effi.rpc.context;

import io.effi.rpc.config.HierarchicalConfig;

import java.util.Collection;

/**
 * Manages a collection of {@link Peer} instance indexed by key.
 */
public interface PeerContainer<P extends Peer> extends HierarchicalConfig.Supplier {

    /**
     * Adds invoker by specified key.
     *
     * @param key     the key associated with the invoker
     * @param invoker the invoker to add
     */
    void addPeer(String key, P peer);

    /**
     * Retrieves invokers by key.
     *
     * @param key the key of the invoker
     * @return the invoker associated with the key, or {@code null} if not found
     */
    P getInvoker(String key);

    /**
     * Retrieves all invokers in the container.
     */
    Collection<P> invokers();

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



