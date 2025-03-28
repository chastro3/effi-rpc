package io.effi.rpc.engine.builder;

import io.effi.rpc.common.extension.ChainBuilder;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.ConfigSource;
import io.effi.rpc.contract.config.ClientConfig;

public abstract class NamedConfigBuilder<T, C extends NamedConfigBuilder<T, C>>
        implements ChainBuilder<T, C>, ConfigSource {

    protected String name;

    protected String protocol;

    protected Config config;

    protected NamedConfigBuilder() {
        config = new Config();
    }

    /**
     * Sets the client name.
     *
     * @param name Client name
     * @return This builder
     */
    public C name(String name) {
        this.name = name;
        return returnThis();
    }

    /**
     * Sets the protocol.
     *
     * @param protocol protocol name
     * @return This builder
     */
    public C protocol(String protocol) {
        this.protocol = protocol;
        return returnThis();
    }

    @Override
    public Config config() {
        return config;
    }

    /**
     * Builds and returns a {@link ClientConfig} instance based on the provided parameters.
     *
     * @return Configured {@link ClientConfig} instance
     */
    @Override
    public T build() {
        return build(config());
    }

    /**
     * Creates a new {@link ClientConfig} instance with the specified URL.
     *
     * @param config Configured config for the client
     * @return New {@link ClientConfig} instance
     */
    protected abstract T build(Config config);
}
