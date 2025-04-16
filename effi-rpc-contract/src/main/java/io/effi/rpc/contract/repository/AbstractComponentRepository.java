package io.effi.rpc.contract.repository;

import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of {@link ComponentRepository}.
 *
 * @param <T> the type of components managed
 */
public abstract class AbstractComponentRepository<T> implements ComponentRepository<T> {

    protected final EffiRpcModule module;

    protected final Map<String, T> components = new HashMap<>();

    protected AbstractComponentRepository(EffiRpcModule module) {
        this.module = module;
    }

    @Override
    public void register(String key, T component) {
        components.put(key, component);
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
