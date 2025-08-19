package io.effi.rpc.protocol.http;

import io.effi.rpc.config.Config;
import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.DefaultClientConfig;
import io.effi.rpc.component.transport.ProtocolStack;

/**
 * Provide a standard http implementation of {@link ClientConfig}.
 */
public class HttpClientConfig extends DefaultClientConfig {

    public HttpClientConfig(String id, Config config, String protocol, CertificateConfig certificateConfig) {
        super(id, config, protocol, ProtocolStack.TCP, certificateConfig);
    }
}
