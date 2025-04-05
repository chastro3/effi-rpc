package io.effi.rpc.contract.manager;

import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of {@link ComponentManager}.
 *
 * @param <T> the type of values managed
 */
public abstract class AbstractComponentManager<T> implements ComponentManager<T> {

    protected final EffiRpcModule module;

    protected final Map<String, T> components = new HashMap<>();

    protected AbstractComponentManager(EffiRpcModule module) {
        this.module = module;
    }

    @Override
    public void register(String key, T value) {
        components.put(key, value);
    }

    @Override
    public void remove(String key) {
        components.remove(key);
    }

    public T get(String key) {
        return components.get(key);
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    @Override
    public Collection<T> components() {
        return components.values();
    }

    @Override
    public void clear() {
        components.clear();
    }

    @Override
    public String toString() {
        return "components=" + components.size();
    }
}
