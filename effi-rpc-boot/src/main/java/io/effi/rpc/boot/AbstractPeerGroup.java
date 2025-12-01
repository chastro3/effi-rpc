package io.effi.rpc.boot;

import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.PeerGroup;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an abstract implementation of {@link PeerGroup}.
 */
public abstract class AbstractPeerGroup<P extends Peer, T> implements PeerGroup<P, T> {

    protected Class<T> targetType;

    protected T target;

    protected Map<String, P> values = new HashMap<>();

    protected HierarchicalOptions options;

    protected void onInitialized(Class<T> targetType, T target) {
        this.targetType = targetType;
        this.target = target;
    }


    @Override
    public Class<T> targetType() {
        return targetType;
    }

    @Override
    public T target() {
        return target;
    }

    @Override
    public void register(P peer) {
        values.put(peer.id(), peer);
        peer.options().withParent(this);
    }

    @Override
    public P lookup(String id) {
        return values.get(id);
    }

    @Override
    public HierarchicalOptions options() {
        return options;
    }

    @Override
    public Collection<P> values() {
        return Collections.unmodifiableCollection(values.values());
    }

}
