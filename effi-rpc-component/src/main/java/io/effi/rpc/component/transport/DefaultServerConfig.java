package io.effi.rpc.component.transport;

import io.effi.rpc.config.Config;

/**
 * Provide the default implementation of {@link ServerConfig}.
 */
public class DefaultServerConfig extends AbstractEndpointConfig implements ServerConfig {

    public DefaultServerConfig(String id, Config config, String protocol,
                               ProtocolStack protocolStack, CertificateConfig certificateConfig) {
        super(id, config, protocol, protocolStack, certificateConfig);
    }

}


