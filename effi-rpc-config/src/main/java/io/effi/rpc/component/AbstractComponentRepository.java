package io.effi.rpc.component;

import io.effi.rpc.util.resoruce.Cleanable;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides an abstract implementation of {@link ComponentRepository}.
 */
public class AbstractComponentRepository<K, T> implements ComponentRepository<K, T> {

    protected final Map<K, T> components = new HashMap<>();

    @Override
    public ComponentRepository<K, T> register(K key, T component) {
        components.put(key, component);
        return this;
    }

    @Override
    public T computeIfAbsent(K key, Function<K, T> function) {
        return components.computeIfAbsent(key, function);
    }

    @Override
    public ComponentRepository<K, T> remove(K key) {
        components.remove(key);
        return this;
    }

    @Override
    public T lookup(K key) {
        return components.get(key);
    }

    @Override
    public int size() {
        return components.size();
    }

    @Override
    public Collection<T> components() {
        return Collections.unmodifiableCollection(components.values());
    }

    @Override
    public Map<K, T> toMap() {
        return Collections.unmodifiableMap(components);
    }

    @Override
    public void clear() {
        for (T component : components()) {
            if (component instanceof Cleanable cleanable) {
                cleanable.clear();
            } else if (component instanceof Closeable closeable) {
                closeable.close();
            }
        }
        components.clear();
    }

    @Override
    public String toString() {
        return "components=" + components.size();
    }
}
