package io.effi.rpc.util;

import io.effi.rpc.util.resoruce.Cleanable;

import java.util.HashMap;
import java.util.Map;

/**
 * todo 待优化
 * Store data for type String.
 *
 * @param <T> current instance
 */
@SuppressWarnings("unchecked")
public abstract class StringAccessor<T> implements Cleanable {

    protected Map<String, String> accessor = new HashMap<>();

    /**
     * Set data.
     */
    public T set(String key, String value) {
        accessor.put(key, value);
        return (T) this;
    }

    /**
     * Remove data.
     */
    public T remove(String key) {
        accessor.remove(key);
        return (T) this;
    }

    /**
     * Get data.
     */
    public String get(String key) {
        return accessor.get(key);
    }

    @Override
    public void clear() {
        accessor.clear();
    }
}
