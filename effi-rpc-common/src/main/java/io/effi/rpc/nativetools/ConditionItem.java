package io.effi.rpc.nativetools;

import io.effi.rpc.constant.EffiRpcFramework;

import java.util.Map;

/**
 * Represents a condition item for type reachability in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/config-condition-schema-v1.0.0.json">config-condition-schema-v1.0.0.json</a>
 */
public class ConditionItem implements NativeConfig.Item {

    private String typeReached;

    public ConditionItem typeReached(String typeReached) {
        this.typeReached = typeReached;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        // https://www.graalvm.org/release-notes/JDK_23
        // Replaced typeReachable conditions with typeReached
        int javaVersion = EffiRpcFramework.javaVersion();
        return MapBuilder.create(2)
                .putIf(javaVersion > 22, "typeReached", typeReached)
                .putIf(javaVersion < 23, "typeReachable", typeReached)
                .build();
    }
}

