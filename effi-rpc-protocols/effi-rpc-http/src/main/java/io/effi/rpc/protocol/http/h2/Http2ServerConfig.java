package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.transport.CertificateConfig;
import io.effi.rpc.config.transport.TrafficShapingConfig;
import io.effi.rpc.protocol.http.HttpServerConfig;
import io.effi.rpc.protocol.http.HttpServerConfigBuilder;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;

import static io.effi.rpc.constant.Component.Protocol.HTTP_2;

/**
 * Implements {@link io.effi.rpc.config.transport.ServerConfig} using http2.
 */
public class Http2ServerConfig extends HttpServerConfig {

    private final Http1ServerConfig h1ServerConfig;

    Http2ServerConfig(String protocol, String name, Config config, CertificateConfig certificateConfig,
                      TrafficShapingConfig trafficShapingConfig, Http1ServerConfig h1ServerConfig) {
        super(protocol, name, config, certificateConfig, trafficShapingConfig);
        this.h1ServerConfig = h1ServerConfig;
    }

    public static Http2ServerConfig defaultConfig() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Http1ServerConfig http1ServerConfig() {
        return h1ServerConfig;
    }

    /**
     * Builds {@link Http2ServerConfig} instance.
     */
    public static class Builder extends HttpServerConfigBuilder<Http2ServerConfig, Builder>
            implements Http2EndpointConfigBuilder<Http2ServerConfig, Builder> {

        private Http1ServerConfig h1ServerConfig;

        public Builder() {
            protocol(HTTP_2);
        }

        public Builder h1ServerConfig(Http1ServerConfig h1ServerConfig) {
            this.h1ServerConfig = h1ServerConfig;
            return returnThis();
        }

        @Override
        protected Http2ServerConfig build(Config config) {
            return new Http2ServerConfig(protocol, name, config, certificateConfig, trafficShapingConfig, h1ServerConfig);
        }
    }
}
