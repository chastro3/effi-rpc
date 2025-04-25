package io.effi.rpc.engine;

import io.effi.rpc.config.Config;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.engine.builder.ServerConfigBuilder;

/**
 * Default implementation of {@link ServerConfig}.
 */
public class DefaultServerConfig extends AbstractNamedConfig implements ServerConfig {

    DefaultServerConfig(String protocol, String name, Config config) {
        super(protocol, name, config);
    }

    /**
     * Creates and returns a new {@link DefaultServerConfigBuilder} instance for building
     * {@link DefaultServerConfig} objects using a fluent API.
     *
     * @return a new {@link DefaultServerConfigBuilder} instance
     */
    public static DefaultServerConfigBuilder builder() {
        return new DefaultServerConfigBuilder();
    }

    /**
     * Builder class for {@link DefaultServerConfig}.
     */
    public static class DefaultServerConfigBuilder extends ServerConfigBuilder<DefaultServerConfig, DefaultServerConfigBuilder> {

        @Override
        protected DefaultServerConfig build(Config config) {
            return new DefaultServerConfig(protocol, name, config);
        }
    }
}


