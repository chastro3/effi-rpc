package io.effi.rpc.protocol.http;

import io.effi.rpc.config.Config;
import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.DefaultServerConfig;
import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.component.transport.ServerConfig;

/**
 * Provide a standard http implementation of {@link ServerConfig}.
 */
public class HttpServerConfig extends DefaultServerConfig {

    public HttpServerConfig(String id, Config config, String protocol,
                            CertificateConfig certificateConfig) {
        super(id, config, protocol, ProtocolStack.TCP, certificateConfig);
    }
}
