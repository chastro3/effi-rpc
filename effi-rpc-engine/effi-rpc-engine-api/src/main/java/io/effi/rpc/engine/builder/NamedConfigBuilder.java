package io.effi.rpc.engine.builder;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.ConfigSource;
import io.effi.rpc.common.util.ChainBuilder;

public abstract class NamedConfigBuilder<T, C extends NamedConfigBuilder<T, C>>
        implements ChainBuilder<T, C>, ConfigSource {

    protected String name;

    protected String protocol;

    protected Config config;

    protected NamedConfigBuilder() {
        config = new Config();
    }

    /**
     * Sets the config name.
     */
    public C name(String name) {
        this.name = name;
        return returnThis();
    }

    /**
     * Sets the protocol.
     */
    public C protocol(String protocol) {
        this.protocol = protocol;
        return returnThis();
    }

    @Override
    public Config config() {
        return config;
    }

    @Override
    public T build() {
        return build(config());
    }

    protected abstract T build(Config config);
}
