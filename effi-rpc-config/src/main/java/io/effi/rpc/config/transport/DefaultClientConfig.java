package io.effi.rpc.config.transport;

import io.effi.rpc.config.Config;

/**
 * Provide the default implementation of {@link ClientConfig}.
 */
public class DefaultClientConfig extends AbstractEndpointConfig implements ClientConfig {

    public DefaultClientConfig(String protocol, String name, Config config, ProtocolStack protocolStack,
                               CertificateConfig certificateConfig, TrafficShapingConfig trafficShapingConfig) {
        super(protocol, name, config, protocolStack, certificateConfig, trafficShapingConfig);
    }
}

