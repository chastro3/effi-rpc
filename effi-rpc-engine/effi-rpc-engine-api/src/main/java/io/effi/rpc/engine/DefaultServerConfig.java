package io.effi.rpc.engine;

import io.effi.rpc.config.Config;
import io.effi.rpc.contract.config.CertificateConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.engine.builder.ServerConfigBuilder;

/**
 * Default implementation of {@link ServerConfig}.
 */
public class DefaultServerConfig extends AbstractEndpointConfig implements ServerConfig {

    DefaultServerConfig(String protocol, String name, Config config, CertificateConfig certificateConfig) {
        super(protocol, name, config, certificateConfig);
    }

    public static DefaultServerConfigBuilder builder() {
        return new DefaultServerConfigBuilder();
    }

    /**
     * Builds {@link DefaultServerConfig} instances.
     */
    public static class DefaultServerConfigBuilder extends ServerConfigBuilder<DefaultServerConfig, DefaultServerConfigBuilder> {

        @Override
        protected DefaultServerConfig build(Config config) {
            return new DefaultServerConfig(protocol, name, config, certificateConfig);
        }
    }
}


