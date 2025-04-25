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

    /**
     * Adds a key-value pair to the map, with special handling for certain types.
     * - If the value is an {@link Item}, its map representation will be added.
     * - If the value is a {@link List}, and it contains {@link Item}s, their map representations will be added.
     * - Boolean values are only added if they are true.
     *
     * @param key   the key to be added.
     * @param value the value associated with the key.
     * @return this MapBuilder instance for method chaining.
     */
    @SuppressWarnings("unchecked")
    public MapBuilder put(String key, Object value) {
        if (value != null) {
            switch (value) {
                case Item item -> map.put(key, item.toMap());
                case List<?> list -> {
                    if (!list.isEmpty()) {
                        if (list.getFirst() instanceof Item) {
                            map.put(key, Item.toMapList((List<? extends Item>) list));
                        } else {
                            map.put(key, value);
                        }
                    }
                }
                case Boolean b -> {
                    if (b) map.put(key, true);
                }
                default -> map.put(key, value);
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

