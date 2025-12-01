package io.effi.rpc.context;

import io.effi.rpc.config.HierarchicalOptions;

import java.util.Collection;

/**
 * Manages a collection of {@link Peer} instance indexed by key.
 */
public interface PeerGroup<P extends Peer, T> extends HierarchicalOptions.Supplier {

    Class<T> targetType();

    T target();

    void register(P peer);

    P lookup(String id);

    <R> R invoke(P peer, Object... args);

    Collection<P> values();

}



