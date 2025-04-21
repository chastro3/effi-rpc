package io.effi.rpc.nativetools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a proxy configuration item in proxy-config.
 */
public class ProxyConfigItem implements Item {

    private ConditionItem condition;

    private final List<String> interfaces = new ArrayList<>();

    public ProxyConfigItem condition(ConditionItem condition) {
        this.condition = condition;
        return this;
    }

    public ProxyConfigItem addInterface(String interfaceName) {
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
