package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.config.Config;
import io.effi.rpc.protocol.http.HttpServerConfig;
import io.effi.rpc.protocol.http.HttpServerConfigBuilder;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;

import static io.effi.rpc.config.ConfigValues.Protocol.HTTP_2;

/**
 * Implements {@link ServerConfig} using http2.
 */
public class Http2ServerConfig extends HttpServerConfig {

    private final Http1ServerConfig h1ServerConfig;

    Http2ServerConfig(String id, Config config, CertificateConfig certificateConfig, Http1ServerConfig h1ServerConfig) {
        super(id, config, HTTP_2, certificateConfig);
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


        public Builder h1ServerConfig(Http1ServerConfig h1ServerConfig) {
            this.h1ServerConfig = h1ServerConfig;
            return self();
        }

        @Override
        public Http2ServerConfig build() {
            return new Http2ServerConfig(id, config, certificateConfig, h1ServerConfig);
        }
    }
}
