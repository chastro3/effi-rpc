package io.effi.rpc.nativetools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a configuration for serialization in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/serialization-config-schema-v1.1.0.json">serialization-config-schema-v1.1.0.json</a>
 */
public class SerializationConfig implements NativeConfig<Map<String, Object>> {

    private final List<TypeItem> types = new ArrayList<>();

    private final List<LambdaCapturingTypeItem> lambdaCapturingTypes = new ArrayList<>();

    private final List<ProxyItem> proxies = new ArrayList<>();

    public SerializationConfig addType(ConditionItem condition, String type) {
        types.add(new TypeItem(condition, type));
        return this;
    }

    public SerializationConfig addLambdaCapturingProxy(ConditionItem condition, String name) {
        lambdaCapturingTypes.add(new LambdaCapturingTypeItem(condition, name));
        return this;
    }

    public SerializationConfig addProxy(ConditionItem condition, List<String> interfaces) {
        proxies.add(new ProxyItem(condition, interfaces));
        return this;
    }

    @Override
    public String name() {
        return "serialization-config.json";
    }

    @Override
    public Map<String, Object> toJsonConfig() {
        return MapBuilder.create(3)
                .put("types", types)
                .put("lambdaCapturingTypes", lambdaCapturingTypes)
                .put("proxies", proxies)
                .build();
    }

    @Override
    public boolean hasResource() {
        return !types.isEmpty() || !lambdaCapturingTypes.isEmpty() || !proxies.isEmpty();
    }

    record TypeItem(ConditionItem condition, String type) implements Item {
        @Override
        public Map<String, Object> toMap() {
            return MapBuilder.create(3)
                    .put("condition", condition)
                    .put("type", type)
                    .put("name", type)
                    .build();
        }
    }

    record LambdaCapturingTypeItem(ConditionItem condition, String name) implements Item {
        @Override
        public Map<String, Object> toMap() {
            return MapBuilder.create(2)
                    .put("condition", condition)
                    .put("name", name)
                    .build();
        }
    }

    record ProxyItem(ConditionItem condition, List<String> interfaces) implements Item {
        @Override
        public Map<String, Object> toMap() {
            return MapBuilder.create(2)
                    .put("condition", condition)
                    .put("interfaces", interfaces)
                    .build();
        }
    }
}
