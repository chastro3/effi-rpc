package io.effi.rpc.config.v2;

import io.effi.rpc.util.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a flat key-value configuration.
 */
public class FlatConfig implements Config {

    protected Object owner;

    protected Map<String, Object> items;

    public FlatConfig() {
        this(16, null);
    }

    public FlatConfig(Object owner) {
        this(16, owner);
    }

    public FlatConfig(int initialCapacity, Object owner) {
        this.items = new HashMap<>(initialCapacity);
        this.owner = owner;
    }

    @Override
    public <V> void set(ConfigName<V> name, V value) {
        set(name.name(), value);
    }

    @Override
    public <V> V get(ConfigName<V> name) {
        return get(name.name());
    }


    @Override
    public <V> V remove(ConfigName<V> name) {
        return remove(name.name());
    }

    @SuppressWarnings("unchecked")
    public <V> V get(String name) {
        return (V) items.get(name);
    }

    public void set(String name, Object value) {
        items.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <V> V remove(String name) {
        return (V) items.remove(name);
    }

    @Override
    public Map<String, Object> items() {
        return Collections.unmodifiableMap(items);
    }

    @Override
    public Object owner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return StringUtil.format("size={}, hasOwner={}", items.size(), owner != null);
    }
}
