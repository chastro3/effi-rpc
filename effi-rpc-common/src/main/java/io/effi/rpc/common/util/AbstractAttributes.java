package io.effi.rpc.common.util;

import io.effi.rpc.common.util.collection.LazyMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Abstract implementation of {@link Attributes}.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractAttributes implements Attributes {

    protected Map<GenericKey<?>, Object> attributes = new LazyMap<>(ConcurrentHashMap::new);

    @Override
    public <T> T get(GenericKey<T> key) {
        return (T) attributes.get(key);
    }

    @Override
    public <T> T getOrDefault(GenericKey<T> key, T defaultValue) {
        T value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public <T> T computeIfAbsent(GenericKey<T> key, Supplier<T> creator) {
        return (T) attributes.computeIfAbsent(key, k -> creator.get());
    }

    @Override
    public <T> T set(GenericKey<T> key, T value) {
        return (T) attributes.computeIfAbsent(key, k -> value);
    }

    @Override
    public Attributes remove(GenericKey<?> key) {
        attributes.remove(key);
        return this;
    }

    @Override
    public void clear() {
        attributes.clear();
    }
}
