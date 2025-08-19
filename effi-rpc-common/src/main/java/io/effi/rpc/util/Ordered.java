package io.effi.rpc.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Define objects with order values for precedence-based sorting.
 * <p>
 * Provides ordering capabilities for objects with lower values indicating
 * higher priority, enabling sorted collections and maps based on precedence.
 */
public interface Ordered {

    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    int DEFAULT = 5;

    /**
     * Sort {@link Ordered} objects by order values.
     */
    static <T extends Ordered> List<T> sort(List<T> values) {
        if (CollectionUtil.isEmpty(values)) {
            return values;
        }
        values.sort(Comparator.comparingInt(Ordered::order));
        return values;
    }

    static <K, V extends Ordered, R> Map<K, R> sort(Map<K, V> map, Function<V, R> mapper) {
        if (CollectionUtil.isEmpty(map)) {
            return Collections.emptyMap();
        }
        List<Map.Entry<K, V>> entries = new ArrayList<>(map.size());
        entries.addAll(map.entrySet());
        entries.sort(Comparator.comparingInt(e -> e.getValue().order()));
        LinkedHashMap<K, R> result = new LinkedHashMap<>(map.size());
        for (Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return result;
    }

    default int order() {
        return DEFAULT;
    }
}



