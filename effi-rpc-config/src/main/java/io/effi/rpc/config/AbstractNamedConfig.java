package io.effi.rpc.config;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;

/**
 * Provides an abstract implementation of {@link NamedConfig}.
 */
public abstract class AbstractNamedConfig implements NamedConfig {

    protected String protocol;

    protected String name;

    protected Config config;

    protected EffiRpcPlatform platform;

    protected AbstractNamedConfig(String protocol, String name, Config config) {
        this.protocol = AssertUtil.notBlank(protocol, "protocol");
        this.config = AssertUtil.notNull(config, "config");
        this.name = name;
        if (StringUtil.isNotBlank(name)) {
            config.set(KeyConstant.NAME, name);
        }
        config.set(KeyConstant.PROTOCOL, protocol);
    }

    public String protocol() {
        return protocol;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Config config() {
        return config;
    }

}
