package io.effi.rpc.config.registry;

import io.effi.rpc.config.AbstractNamedConfig;
import io.effi.rpc.config.URL;

/**
 * Provides the default implementation of {@link RegistryConfig}.
 */
public class DefaultRegistryConfig extends AbstractNamedConfig implements RegistryConfig {

    protected URL url;

    DefaultRegistryConfig(String protocol, String name, URL url) {
        super(protocol, name, url.params());
        this.url = url;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public URL url() {
        return url;
    }

    @Override
    public String toString() {
        return url().toString();
    }

    /**
     * Builds {@link DefaultRegistryConfig} instance.
     */
    public static class Builder extends RegistryConfigBuilder<DefaultRegistryConfig, Builder> {

        @Override
        protected DefaultRegistryConfig build(URL url) {
            return new DefaultRegistryConfig(protocol, name, url);
        }
    }
}

