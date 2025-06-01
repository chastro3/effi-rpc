package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.transport.CertificateConfig;
import io.effi.rpc.config.transport.TrafficShapingConfig;
import io.effi.rpc.protocol.http.HttpClientConfig;
import io.effi.rpc.protocol.http.HttpClientConfigBuilder;

import static io.effi.rpc.constant.Component.Protocol.HTTP_1_1;

/**
 * Implements {@link io.effi.rpc.config.transport.ClientConfig} using http1.1.
 */
public class Http1ClientConfig extends HttpClientConfig {

    private static final Http1ClientConfig DEFAULT_CONFIG = builder().name("default-htt1").build();

    Http1ClientConfig(String protocol, String name, Config config, CertificateConfig certificateConfig, TrafficShapingConfig trafficShapingConfig) {
        super(protocol, name, config, certificateConfig, trafficShapingConfig);
    }

    public static Http1ClientConfig defaultConfig() {
        return DEFAULT_CONFIG;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds {@link Http1ClientConfig} instance.
     */
    public static class Builder extends HttpClientConfigBuilder<Http1ClientConfig, Builder>
            implements Http1EndpointConfigBuilder<Http1ClientConfig, Builder> {

        public Builder() {
            protocol(HTTP_1_1);
        }

        @Override
        protected Http1ClientConfig build(Config config) {
            return new Http1ClientConfig(protocol, name, config, certificateConfig, trafficShapingConfig);
        }
    }
}
