package io.effi.rpc.protocol.http;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.ClientConfigBuilder;
import io.effi.rpc.component.transport.TcpEndpointConfigBuilder;
import io.effi.rpc.config.IdentifiableConfigBuilder;

/**
 * Builds {@link HttpClientConfig} instance and defines configuration.
 */
public abstract class HttpClientConfigBuilder<T extends HttpClientConfig, SELF extends HttpClientConfigBuilder<T, SELF>>
        extends IdentifiableConfigBuilder<T, SELF> implements ClientConfigBuilder<T, SELF>, TcpEndpointConfigBuilder<T, SELF>, HttpEndpointConfigBuilder<T, SELF> {

    protected CertificateConfig certificateConfig;

    @Override
    public SELF certificate(CertificateConfig certificateConfig) {
        this.certificateConfig = certificateConfig;
        return self();
    }
}
