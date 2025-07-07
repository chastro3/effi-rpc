package io.effi.rpc.boot;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.CallSideContainer;
import io.effi.rpc.config.NodeConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an abstract implementation of {@link CallSideContainer}.
 */
public abstract class AbstractCallSideContainer<I extends CallSide> implements CallSideContainer<I> {

    protected Map<String, I> invokers = new HashMap<>();

    protected NodeConfig config;

    @Override
    public void addInvoker(String key, I invoker) {
        invokers.put(key, invoker);
    }

    @Override
    public I getInvoker(String key) {
        return invokers.get(key);
    }

    @Override
    public NodeConfig config() {
        return config;
    }

    @Override
    public Collection<I> invokers() {
        return Collections.unmodifiableCollection(invokers.values());
    }

}
