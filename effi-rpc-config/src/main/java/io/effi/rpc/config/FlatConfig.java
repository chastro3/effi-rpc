package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a flat key-value configuration.
 */
public class FlatConfig implements Config {

    protected Object owner;

    protected Map<String, String> items;

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
    public String get(ConfigName key) {
        return getOrDefault(key.realName(), key.defaultValue());
    }

    @Override
    public String get(String key) {
        return items.get(key);
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        return items.getOrDefault(key, defaultValue);
    }

    @Override
    public void set(ConfigName key, String value) {
        set(key.realName(), value);
    }

    @Override
    public void set(String key, String value) {
        if (value != null) {
            items.put(key, value);
        }
    }

    @Override
    public void set(Map<String, String> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.items.putAll(items);
        }
    }

    @Override
    public void remove(String key) {
        items.remove(key);
    }

    @Override
    public Map<String, String> items() {
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
