package io.effi.rpc.context;

import io.effi.rpc.option.HierarchicalOptions;

import java.util.Collection;

/**
 * Manages a collection of {@link Peer} instance indexed by key.
 */
public interface PeerGroup<P extends Peer, T> extends HierarchicalOptions.Supplier {

    Class<T> targetType();

    void register(P peer);

    P lookup(String id);

    Collection<P> values();

}



