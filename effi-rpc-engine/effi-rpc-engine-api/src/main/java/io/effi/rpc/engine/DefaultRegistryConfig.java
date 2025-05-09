package io.effi.rpc.engine;

import io.effi.rpc.config.URL;
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

    public static DefaultRegistryConfigBuilder builder() {
        return new DefaultRegistryConfigBuilder();
    }

    @Override
    public URL url() {
        return url;
    }

    /**
     * Builds {@link DefaultRegistryConfig} instances.
     */
    public static class DefaultRegistryConfigBuilder extends RegistryConfigBuilder<DefaultRegistryConfig, DefaultRegistryConfigBuilder> {

        @Override
        protected DefaultRegistryConfig build(URL url) {
            return new DefaultRegistryConfig(protocol, name, url);
        }
    }
}

