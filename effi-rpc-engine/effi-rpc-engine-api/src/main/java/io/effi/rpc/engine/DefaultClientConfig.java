package io.effi.rpc.engine;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.engine.builder.ClientConfigBuilder;

/**
 * Default implementation of {@link ClientConfig}.
 */
public class DefaultClientConfig extends AbstractNamedConfig implements ClientConfig {

    DefaultClientConfig(String protocol, String name, Config config) {
        super(protocol, name, config);
    }

    /**
     * Creates and returns a new {@link DefaultClientConfigBuilder} instance for constructing
     * {@link DefaultClientConfig} objects using a fluent API.
     *
     * @return a new {@link DefaultClientConfigBuilder} instance
     */
    public static DefaultClientConfigBuilder builder() {
        return new DefaultClientConfigBuilder();
    }

    /**
     * Builder class for {@link DefaultClientConfig}.
     */
    public static class DefaultClientConfigBuilder extends ClientConfigBuilder<DefaultClientConfig, DefaultClientConfigBuilder> {

        @Override
        protected DefaultClientConfig build(Config config) {
            return new DefaultClientConfig(protocol, name, config);
        }
    }
}

