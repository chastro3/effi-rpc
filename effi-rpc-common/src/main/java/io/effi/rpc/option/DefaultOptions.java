package io.effi.rpc.option;

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

    /**
     * Returns the shared empty options instance.
     */
    public static Options empty() {
        return EMPTY;
    }

    @Override
    public <T> Options addOption(OptionName<T> name, T value) {
        if (value != null) {
            items.put(name.name(), value);
        }
        return this;
    }

    @Override
    public <T> T option(OptionName<T> name) {
        T value = currentOption(name);
        return value == null ? name.defaultValue() : value;
    }

    @Override
    public <T> T removeOption(OptionName<T> name) {
        Object value = items.remove(name.name());
        return value == null ? null : name.type().convert(value);
    }

    /**
     * Returns an unmodifiable view of all option values.
     */
    @Override
    public Map<String, Object> items() {
        return Collections.unmodifiableMap(items);
    }

    protected <T> T currentOption(OptionName<T> name) {
        Object value = items.get(name.name());
        return value == null ? null : name.type().convert(value);
    }

    @Override
    public String toString() {
        return StringUtil.format("size={}", items.size());
    }
}
