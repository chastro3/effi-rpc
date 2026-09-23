package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.option.Options;


/**
 * Implements {@link ServerConfig} using http1.1.
 */
public class Http1ServerConfig extends Http1EndpointConfig implements ServerConfig {

    Http1ServerConfig(String id, Options options, CertificateConfig certificateConfig) {
        super(id, options, certificateConfig);
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
    public static class Builder extends EndpointConfig.Builder<Http1ServerConfig, Builder>
            implements Http1EndpointConfig.Configurator<Http1ServerConfig.Builder> {

        @Override
        public Http1ServerConfig build() {
            return new Http1ServerConfig(id, options, certificateConfig);
        }
    }
}
