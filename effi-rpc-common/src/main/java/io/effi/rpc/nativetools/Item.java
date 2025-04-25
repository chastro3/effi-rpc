package io.effi.rpc.nativetools;

import io.effi.rpc.util.CollectionUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a configuration entry in native config.
 */
public interface Item {

    /**
     * Converts the current configuration entry to a map representation.
     */
    Map<String, Object> toMap();

    /**
     * Converts a list of configuration entries to a list of maps.
     */
    static <T extends Item> List<Map<String, Object>> toMapList(List<T> list) {
        return CollectionUtil.isEmpty(list) ? Collections.emptyList()
                : list.stream().map(Item::toMap).toList();
    }
}


