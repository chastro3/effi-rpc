package io.effi.rpc.engine;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.engine.builder.RegistryConfigBuilder;

/**
 * Default implementation of {@link RegistryConfig}.
 */
public class DefaultRegistryConfig extends AbstractNamedConfig implements RegistryConfig {

    protected URL url;

    DefaultRegistryConfig(String protocol, String name, URL url) {
        super(protocol, name, url.params());
        this.url = url;
    }

    /**
     * Creates and returns a new {@link DefaultRegistryConfigBuilder} instance for constructing
     * {@link DefaultRegistryConfig} objects using a fluent API.
     *
     * @return a new {@link DefaultRegistryConfigBuilder} instance
     */
    public static DefaultRegistryConfigBuilder builder() {
        return new DefaultRegistryConfigBuilder();
    }

    @Override
    public URL url() {
        return url;
    }

    /**
     * Builder class for {@link DefaultRegistryConfig}.
     */
    public static class DefaultRegistryConfigBuilder extends RegistryConfigBuilder<DefaultRegistryConfig, DefaultRegistryConfigBuilder> {

        @Override
        protected DefaultRegistryConfig build(URL url) {
            return new DefaultRegistryConfig(protocol, name, url);
        }
    }
}

