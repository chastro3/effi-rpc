package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.transport.CertificateConfig;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.transport.TrafficShapingConfig;
import io.effi.rpc.protocol.http.HttpServerConfig;
import io.effi.rpc.protocol.http.HttpServerConfigBuilder;

import static io.effi.rpc.constant.Component.Protocol.HTTP_1_1;

/**
 * Implements {@link io.effi.rpc.config.transport.ServerConfig} using http1.1.
 */
public class Http1ServerConfig extends HttpServerConfig {

    Http1ServerConfig(String protocol, String name, Config config, CertificateConfig certificateConfig, TrafficShapingConfig trafficShapingConfig) {
        super(protocol, name, config, certificateConfig, trafficShapingConfig);
    }

    public static Http1ServerConfig defaultConfig() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds {@link Http1ServerConfig} instance.
     */
    public static class Builder extends HttpServerConfigBuilder<Http1ServerConfig, Builder>
            implements Http1EndpointConfigBuilder<Http1ServerConfig, Builder> {

        public Builder() {
            protocol(HTTP_1_1);
        }

        @Override
        protected Http1ServerConfig build(Config config) {
            return new Http1ServerConfig(protocol, name, config, certificateConfig, trafficShapingConfig);
        }
    }
}
