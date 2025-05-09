package io.effi.rpc.engine;

import io.effi.rpc.config.Config;
import io.effi.rpc.contract.config.CertificateConfig;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.engine.builder.ClientConfigBuilder;

/**
 * Default implementation of {@link ClientConfig}.
 */
public class DefaultClientConfig extends AbstractEndpointConfig implements ClientConfig {

    DefaultClientConfig(String protocol, String name, Config config, CertificateConfig certificateConfig) {
        super(protocol, name, config, certificateConfig);
    }

    public static DefaultClientConfigBuilder builder() {
        return new DefaultClientConfigBuilder();
    }

    /**
     * Builds {@link DefaultClientConfig} instances.
     */
    public static class DefaultClientConfigBuilder extends ClientConfigBuilder<DefaultClientConfig, DefaultClientConfigBuilder> {

        @Override
        protected DefaultClientConfig build(Config config) {
            return new DefaultClientConfig(protocol, name, config, certificateConfig);
        }
    }
}

