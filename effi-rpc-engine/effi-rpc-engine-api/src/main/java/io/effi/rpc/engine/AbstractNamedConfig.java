package io.effi.rpc.engine;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.config.NamedConfig;

/**
 * Abstract implementation of {@link NamedConfig}.
 */
public abstract class AbstractNamedConfig implements NamedConfig {

    protected String protocol;

    protected String name;

    protected Config config;

    public AbstractNamedConfig(String protocol, String name, Config config) {
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
