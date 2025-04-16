package io.effi.rpc.nativetools;

import java.util.Map;

public interface Item {

    Map<String, Object> toMap();

    static void fillBoolean(Map<String, Object> map, String key, Boolean value) {
        if (value != null && value) {
            map.put(key, true);
        }
    }
}
