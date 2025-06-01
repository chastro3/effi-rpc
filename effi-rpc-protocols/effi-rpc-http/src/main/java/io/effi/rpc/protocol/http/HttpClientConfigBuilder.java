package io.effi.rpc.protocol.http;

import io.effi.rpc.config.NamedConfigBuilder;
import io.effi.rpc.config.transport.CertificateConfig;
import io.effi.rpc.config.transport.ClientConfigBuilder;
import io.effi.rpc.config.transport.TcpEndpointConfigBuilder;
import io.effi.rpc.config.transport.TrafficShapingConfig;

/**
 * Builds {@link HttpClientConfig} instance and defines configuration.
 */
public abstract class HttpClientConfigBuilder<T extends HttpClientConfig, C extends HttpClientConfigBuilder<T, C>>
        extends NamedConfigBuilder<T, C> implements ClientConfigBuilder<T, C>, TcpEndpointConfigBuilder<T, C>, HttpEndpointConfigBuilder<T, C> {

    protected TrafficShapingConfig trafficShapingConfig;

    protected CertificateConfig certificateConfig;

    @Override
    public C trafficShapingOptions(TrafficShapingConfig trafficShapingConfig) {
        this.trafficShapingConfig = trafficShapingConfig;
        return returnThis();
    }

    @Override
    public C certificate(CertificateConfig certificateConfig) {
        this.certificateConfig = certificateConfig;
        return returnThis();
    }
}
