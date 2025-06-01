package io.effi.rpc.nativetools;

import java.util.Map;

/**
 * Represents a condition item for type reachability in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/config-condition-schema-v1.0.0.json">config-condition-schema-v1.0.0.json</a>
 */
public class ConditionItem implements Item {

    //@Deprecated
    private String typeReachable;

    private String typeReached;

    //@Deprecated
    public ConditionItem typeReachable(String typeReachable) {
        this.typeReachable = typeReachable;
        return this;
    }

    public ConditionItem typeReached(String typeReached) {
        this.typeReached = typeReached;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        return MapBuilder.create(2)
                .put("typeReachable", typeReachable)
                .put("typeReached", typeReached)
                .build();
    }
}

