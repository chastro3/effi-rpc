package io.effi.rpc.nativetools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a configuration for reflection in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/reflect-config-schema-v1.1.0.json">reflect-config-schema-v1.1.0.json</a>
 */
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
        return Item.toMapList(items);
    }

    @Override
    public boolean hasResource() {
        return !items.isEmpty();
    }
}
