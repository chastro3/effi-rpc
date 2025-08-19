package io.effi.rpc.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Provides an abstract implementation of {@link Attributes}.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractAttributes implements Attributes {

    private final Object lock = new Object();

    private volatile Map<GenericKey<?>, Object> attributes;

    @Override
    public <T> T get(GenericKey<T> key) {
        return (T) delayedAttributes().get(key);
    }

    @Override
    public <T> T getOrDefault(GenericKey<T> key, T defaultValue) {
        T value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public <T> T computeIfAbsent(GenericKey<T> key, Supplier<T> creator) {
        return (T) delayedAttributes().computeIfAbsent(key, k -> creator.get());
    }

    @Override
    public <T> T set(GenericKey<T> key, T value) {
        return (T) delayedAttributes().computeIfAbsent(key, k -> value);
    }

    @Override
    public Attributes remove(GenericKey<?> key) {
        delayedAttributes().remove(key);
        return this;
    }

    @Override
    public void clear() {
        if (attributes != null)
            attributes.clear();
    }

    @Override
    public String toString() {
        return "attributes=" + (attributes == null ? 0 : attributes.size());
    }

    protected Map<GenericKey<?>, Object> delayedAttributes() {
        Map<GenericKey<?>, Object> result = attributes;
        if (result != null) return result;
        synchronized (lock) {
            result = attributes;
            return result != null ? result : (attributes = new ConcurrentHashMap<>());
        }
    }
}
