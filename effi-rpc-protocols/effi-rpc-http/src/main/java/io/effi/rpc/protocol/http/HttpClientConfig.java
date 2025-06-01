package io.effi.rpc.protocol.http;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.transport.*;

/**
 * Provide a standard http implementation of {@link ClientConfig}.
 */
public class HttpClientConfig extends DefaultClientConfig {

    public HttpClientConfig(String protocol, String name, Config config,
                            CertificateConfig certificateConfig, TrafficShapingConfig trafficShapingConfig) {
        super(protocol, name, config, ProtocolStack.TCP, certificateConfig, trafficShapingConfig);
    }
}
