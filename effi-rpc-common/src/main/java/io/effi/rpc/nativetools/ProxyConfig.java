package io.effi.rpc.nativetools;

import io.effi.rpc.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a configuration for proxy in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/proxy-config-schema-v1.0.0.json">proxy-config-schema-v1.1.0.json</a>
 */
public class ProxyConfig implements NativeConfig<List<Map<String, Object>>> {

    private final List<ProxyConfigItem> items = new ArrayList<>();

    public void addItem(ProxyConfigItem item) {
        if (item != null) items.add(item);
    }

    @Override
    public String name() {
        return "proxy-config.json";
    }

    @Override
    public List<Map<String, Object>> toJsonConfig() {
        return Item.toMapList(items);
    }

    @Override
    public boolean hasResource() {
        return CollectionUtil.isNotEmpty(items);
    }
}
