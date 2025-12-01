package io.effi.rpc.config;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the default implementation of {@link Options}.
 */
public class DefaultOptions implements Options {

    private static final DefaultOptions EMPTY = new DefaultOptions(Collections.emptyMap());

    protected Map<String, Object> items;

    public DefaultOptions() {
        this(16);
    }


    public DefaultOptions(int initialCapacity) {
        this(new HashMap<>(initialCapacity));
    }

    public DefaultOptions(Map<String, Object> items) {
        this.items = AssertUtil.notNull(items, "items");
    }

    public static Options empty() {
        return EMPTY;
    }

    @Override
    public <V> Options addOption(OptionName<V> name, V value) {
        addOption(name.name(), value);
        return this;
    }

    @Override
    public Options addOption(String name, Object value) {
        if (value != null)
            items.put(name, value);
        return this;
    }

    @Override
    public <V> V option(OptionName<V> name) {
        V value = option(name.name());
        return value == null ? name.defaultValue() : value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V option(String name) {
        return (V) items.get(name);
    }

    @Override
    public <V> V removeOption(OptionName<V> name) {
        return removeOption(name.name());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V removeOption(String name) {
        return (V) items.remove(name);
    }

    @Override
    public Map<String, Object> items() {
        return Collections.unmodifiableMap(items);
    }

    @Override
    public String toString() {
        return StringUtil.format("size={}", items.size());
    }
}
