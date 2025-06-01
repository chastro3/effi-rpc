package io.effi.rpc.protocol.http;

import io.effi.rpc.config.transport.CertificateConfig;
import io.effi.rpc.config.NamedConfigBuilder;
import io.effi.rpc.config.transport.TrafficShapingConfig;
import io.effi.rpc.config.transport.ServerConfigBuilder;
import io.effi.rpc.config.transport.TcpEndpointConfigBuilder;

/**
 * Builds {@link HttpServerConfig} instance and defines configuration.
 */
public abstract class HttpServerConfigBuilder<T extends HttpServerConfig, C extends HttpServerConfigBuilder<T, C>>
        extends NamedConfigBuilder<T, C> implements ServerConfigBuilder<T, C>, TcpEndpointConfigBuilder<T, C>, HttpEndpointConfigBuilder<T, C> {

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
