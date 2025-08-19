package io.effi.rpc.nativetools;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Constructs a map with various types of values.
 */
public final class MapBuilder {

    private final Map<String, Object> map;

    private MapBuilder(int size) {
        this.map = new LinkedHashMap<>(size);
    }

    public static MapBuilder create(int size) {
        return new MapBuilder(size);
    }

    public static MapBuilder create() {
        return new MapBuilder(4);
    }

    public MapBuilder putIf(boolean condition, String key, Object value) {
        if (condition) put(key, value);
        return this;
    }

    /**
     * Adds a key-value pair to the map, with special handling for certain types.
     * - If the value is an {@link NativeConfigItem}, its map representation will be added.
     * - If the value is a {@link List}, and it contains {@link NativeConfigItem}s, their map representations will be added.
     * - Boolean values are only added if they are true.
     */
    @SuppressWarnings("unchecked")
    public MapBuilder put(String key, Object value) {
        if (value != null) {
            if (value instanceof NativeConfig.Item item) {
                map.put(key, item.toMap());
            } else if (value instanceof List<?> list) {
                if (!list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof NativeConfig.Item) {
                        map.put(key, NativeConfig.Item.toMapList((List<? extends NativeConfig.Item>) list));
                    } else {
                        map.put(key, value);
                    }
                }
            } else if (value instanceof Boolean b) {
                if (b) map.put(key, true);
            } else {
                map.put(key, value);
            }
        }
        return this;
    }

    public Map<String, Object> build() {
        return map;
    }

    private <T> List<T> orEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}

