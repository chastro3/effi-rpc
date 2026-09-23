package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.option.Options;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;

/**
 * Implements {@link ServerConfig} using http2.
 */
public class Http2ServerConfig extends Http2EndpointConfig implements ServerConfig {

    private final Http1ServerConfig h1ServerConfig;

    Http2ServerConfig(
            String id,
            Options options,
            CertificateConfig certificateConfig,
            Http1ServerConfig h1ServerConfig
    ) {
        super(id, options, certificateConfig);
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
    public static class Builder extends EndpointConfig.Builder<Http2ServerConfig, Builder>
            implements Http2EndpointConfig.Configurator<Builder> {

        private Http1ServerConfig h1ServerConfig;


        public Builder h1ServerConfig(Http1ServerConfig h1ServerConfig) {
            this.h1ServerConfig = h1ServerConfig;
            return self();
        }

        @Override
        public Http2ServerConfig build() {
            return new Http2ServerConfig(id, options, certificateConfig, h1ServerConfig);
        }
    }
}
