package io.effi.rpc.nativetools;

import io.effi.rpc.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a configuration for proxy in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/proxy-config-schema-v1.0.0.json">proxy-config-schema-v1.1.0.json</a>
 */
public class ProxyConfig implements NativeConfig<List<Map<String, Object>>> {

    private final List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        if (item != null) items.add(item);
    }

    @Override
    public String name() {
        return "proxy-config.json";
    }

    @Override
    public List<Map<String, Object>> toJsonConfig() {
        return NativeConfig.Item.toMapList(items);
    }

    @Override
    public boolean hasResource() {
        return CollectionUtil.isNotEmpty(items);
    }
    /**
     * Represents a proxy configuration item in proxy-config.
     */
    public static class Item implements NativeConfig.Item {

        private ConditionItem condition;

        private final List<String> interfaces = new ArrayList<>();

        public Item condition(ConditionItem condition) {
            this.condition = condition;
            return this;
        }

        public Item addInterface(String interfaceName) {
            interfaces.add(interfaceName);
            return this;
        }

        @Override
        public Map<String, Object> toMap() {
            return MapBuilder.create(2)
                    .put("condition", condition)
                    .put("interfaces", interfaces)
                    .build();
        }
    }
}
