package io.effi.rpc.config;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.StringUtil;

/**
 * Provides an abstract base class for identifiable configurations.
 * <p>
 * Combines configuration management with unique identification capabilities,
 * enabling configs to be uniquely identified and accessed through the Config.Supplier interface.
 */
public abstract class IdentifiableConfig implements Identifiable, Config.Supplier {

    private final String id;

    private final Config config;

    protected IdentifiableConfig(String id, Config config) {
        this.id = AssertUtil.notBlank(id, "id");
        this.config = config;
    }

    @Override
    public Config config() {
        return config;
    }

    @Override
    public String id() {
        return id;
    }

    public static String checkId(String id, String prefix) {
        if (StringUtil.isNotBlank(id)) return id;
        return prefix + "-" + Long.toHexString(System.currentTimeMillis());
    }
}
