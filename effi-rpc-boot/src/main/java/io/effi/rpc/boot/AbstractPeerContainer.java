package io.effi.rpc.boot;

import io.effi.rpc.context.Peer;
import io.effi.rpc.context.PeerContainer;
import io.effi.rpc.config.HierarchicalConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an abstract implementation of {@link PeerContainer}.
 */
public abstract class AbstractPeerContainer<I extends Peer> implements PeerContainer<I> {

    protected Map<String, I> invokers = new HashMap<>();

    protected HierarchicalConfig config;

    @Override
    public void addPeer(String key, I invoker) {
        invokers.put(key, invoker);
    }

    @Override
    public I getInvoker(String key) {
        return invokers.get(key);
    }

    @Override
    public HierarchicalConfig config() {
        return config;
    }

    @Override
    public Collection<I> invokers() {
        return Collections.unmodifiableCollection(invokers.values());
    }

}
