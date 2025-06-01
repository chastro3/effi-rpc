package io.effi.rpc.config.transport;

import io.effi.rpc.config.Config;

/**
 * Provide the default implementation of {@link ServerConfig}.
 */
public class DefaultServerConfig extends AbstractEndpointConfig implements ServerConfig {

    public DefaultServerConfig(String protocol, String name, Config config, ProtocolStack protocolStack,
                               CertificateConfig certificateConfig, TrafficShapingConfig trafficShapingConfig) {
        super(protocol, name, config, protocolStack, certificateConfig, trafficShapingConfig);
    }

}


