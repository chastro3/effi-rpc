package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.transport.CertificateConfig;
import io.effi.rpc.config.transport.TrafficShapingConfig;
import io.effi.rpc.protocol.http.HttpClientConfig;
import io.effi.rpc.protocol.http.HttpClientConfigBuilder;

import static io.effi.rpc.constant.Component.Protocol.HTTP_2;

/**
 * Implements {@link io.effi.rpc.config.transport.ClientConfig} using http2.
 */
public class Http2ClientConfig extends HttpClientConfig {

    private static final Http2ClientConfig DEFAULT_CONFIG = builder().name("default-http2").build();

    Http2ClientConfig(String protocol, String name, Config config, CertificateConfig certificateConfig, TrafficShapingConfig trafficShapingConfig) {
        super(protocol, name, config, certificateConfig, trafficShapingConfig);
    }

    public static Http2ClientConfig defaultConfig() {
        return DEFAULT_CONFIG;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds {@link Http2ClientConfig} instance.
     */
    public static class Builder extends HttpClientConfigBuilder<Http2ClientConfig, Builder>
            implements Http2EndpointConfigBuilder<Http2ClientConfig, Builder> {

        public Builder() {
            protocol(HTTP_2);
        }

        @Override
        protected Http2ClientConfig build(Config config) {
            return new Http2ClientConfig(protocol, name, config, certificateConfig, trafficShapingConfig);
        }
    }
}
