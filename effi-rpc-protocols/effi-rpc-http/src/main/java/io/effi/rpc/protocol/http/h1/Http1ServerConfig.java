package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.config.Config;
import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.protocol.http.HttpServerConfig;
import io.effi.rpc.protocol.http.HttpServerConfigBuilder;

import static io.effi.rpc.config.ConfigValues.Protocol.HTTP_1_1;


/**
 * Implements {@link ServerConfig} using http1.1.
 */
public class Http1ServerConfig extends HttpServerConfig {

    Http1ServerConfig(String id, Config config, CertificateConfig certificateConfig) {
        super(id, config, HTTP_1_1, certificateConfig);
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

        @Override
        public Http1ServerConfig build() {
            return new Http1ServerConfig(id, config, certificateConfig);
        }

    }
}
