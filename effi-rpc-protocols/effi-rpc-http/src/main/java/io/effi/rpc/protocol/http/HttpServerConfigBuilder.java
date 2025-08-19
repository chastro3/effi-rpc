package io.effi.rpc.protocol.http;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.config.IdentifiableConfigBuilder;
import io.effi.rpc.component.transport.ServerConfigBuilder;
import io.effi.rpc.component.transport.TcpEndpointConfigBuilder;

/**
 * Builds {@link HttpServerConfig} instance and defines configuration.
 */
public abstract class HttpServerConfigBuilder<T extends HttpServerConfig, SELF extends HttpServerConfigBuilder<T, SELF>>
        extends IdentifiableConfigBuilder<T, SELF> implements ServerConfigBuilder<T, SELF>, TcpEndpointConfigBuilder<T, SELF>, HttpEndpointConfigBuilder<T, SELF> {

    protected CertificateConfig certificateConfig;

    @Override
    public SELF certificate(CertificateConfig certificateConfig) {
        this.certificateConfig = certificateConfig;
        return self();
    }

}
