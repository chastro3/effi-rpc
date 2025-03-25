package io.effi.rpc.contract.manager;

import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of {@link Manager}.
 *
 * @param <T> the type of values managed
 */
public abstract class AbstractManager<T> implements Manager<T> {

    protected final EffiRpcModule module;

    protected final Map<String, T> map = new HashMap<>();

    protected AbstractManager(EffiRpcModule module) {
        this.module = module;
    }

    @Override
    public void register(String key, T value) {
        map.put(key, value);
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    public T get(String key) {
        return map.get(key);
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    @Override
    public Collection<T> values() {
        return map.values();
    }

    @Override
    public void clear() {
        map.clear();
    }

}
