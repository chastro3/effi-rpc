package io.effi.rpc.context;

import io.effi.rpc.option.HierarchicalOptions;

import java.util.Collection;

/**
 * Manages a collection of {@link Peer} instances indexed by key.
 */
public interface PeerGroup<P extends Peer, T> extends HierarchicalOptions.Supplier {

    /**
     * Returns the target service type wrapped by this group.
     */
    Class<T> targetType();

    /**
     * Registers one peer in this group.
     *
     * @param peer peer to register
     */
    void register(P peer);

    /**
     * Returns the peer registered under the supplied id.
     *
     * @param id peer identity
     * @return registered peer
     */
    P lookup(String id);

    /**
     * Returns every registered peer.
     */
    Collection<P> values();

}



