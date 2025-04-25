package io.effi.rpc.engine.builder;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigSource;
import io.effi.rpc.config.FlatConfig;
import io.effi.rpc.util.ChainBuilder;

abstract class NamedConfigBuilder<T, C extends NamedConfigBuilder<T, C>>
        implements ChainBuilder<T, C>, ConfigSource {

    protected String name;

    protected String protocol;

    protected FlatConfig config;

    protected NamedConfigBuilder() {
        config = new FlatConfig();
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
        T instance = build(config());
        config.setOwner(instance);
        return instance;
    }

    protected abstract T build(Config config);
}
