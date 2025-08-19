package io.effi.rpc.config;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the default implementation of {@link Config}.
 */
public class DefaultConfig implements Config {

    private static final DefaultConfig EMPTY = new DefaultConfig(Collections.emptyMap(), null);

    protected Object owner;

    protected Map<String, Object> items;

    public DefaultConfig() {
        this(16, null);
    }

    public DefaultConfig(Object owner) {
        this(new HashMap<>(), owner);
    }

    public DefaultConfig(int initialCapacity, Object owner) {
        this(new HashMap<>(initialCapacity), owner);
    }

    public DefaultConfig(Map<String, Object> items, Object owner) {
        this.items = AssertUtil.notNull(items, "items");
        this.owner = owner;
    }

    public static Config empty() {
        return EMPTY;
    }

    @Override
    public <V> void set(ConfigName<V> name, V value) {
        set(name.name(), value);
    }

    @Override
    public void set(String name, Object value) {
        items.put(name, value);
    }

    @Override
    public <V> V get(ConfigName<V> name) {
        V value = get(name.name());
        return value == null ? name.defaultValue() : value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V get(String name) {
        return (V) items.get(name);
    }

    @Override
    public <V> V remove(ConfigName<V> name) {
        return remove(name.name());
    }

    @SuppressWarnings("unchecked")
    @Override
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

    @Override
    public DefaultConfig withOwner(Object owner) {
        if (this.owner != owner) {
            this.owner = owner;
        }
        return this;
    }

    @Override
    public String toString() {
        return StringUtil.format("size={}, hasOwner={}", items.size(), owner != null);
    }
}
