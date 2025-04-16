package io.effi.rpc.engine;

import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.InvokerContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of {@link InvokerContainer}.
 *
 * @param <I> the type of the invoker
 */
public abstract class AbstractInvokerContainer<I extends Invoker<?>> implements InvokerContainer<I> {

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
