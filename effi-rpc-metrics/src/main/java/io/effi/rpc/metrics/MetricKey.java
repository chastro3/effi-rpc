package io.effi.rpc.metrics;

import io.effi.rpc.util.AssertUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Identifies a metric by name and immutable tags.
 */
public record MetricKey(String name, Map<String, String> tags) {

    public MetricKey {
        name = AssertUtil.notBlank(name, "name");
        tags = tags == null || tags.isEmpty() ? Map.of() : Map.copyOf(tags);
    }

    public static MetricKey of(String name) {
        return new MetricKey(name, Map.of());
    }

    public static MetricKey of(String name, Map<String, String> tags) {
        return new MetricKey(name, tags);
    }

    /**
     * Returns a copy with one additional or replaced tag.
     *
     * @param key tag key
     * @param value tag value
     * @return metric id carrying the supplied tag
     */
    public MetricKey withTag(String key, String value) {
        AssertUtil.notBlank(key, "key");
        AssertUtil.notBlank(value, "value");
        Map<String, String> result = new LinkedHashMap<>(tags);
        result.put(key, value);
        return new MetricKey(name, result);
    }
}
