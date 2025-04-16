package io.effi.rpc.nativetools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReflectConfig implements NativeConfig<List<Map<String, Object>>> {

    private final List<ReflectConfigItem> items = new ArrayList<>();

    public void addItem(ReflectConfigItem item) {
        if (item != null)
            items.add(item);
    }

    @Override
    public String name() {
        return "reflect-config.json";
    }

    @Override
    public List<Map<String, Object>> toJsonConfig() {
        return items.stream().map(ReflectConfigItem::toMap).toList();
    }

    @Override
    public boolean hasResource() {
        return !items.isEmpty();
    }
}
