package io.effi.rpc.config;

import io.effi.rpc.util.FluentBuilder;

/**
 * Builds {@link NamedConfig} instance and defines configuration.
 */
public abstract class NamedConfigBuilder<T, C extends NamedConfigBuilder<T, C>>
        implements FluentBuilder<T, C>, Config.Provider {

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
