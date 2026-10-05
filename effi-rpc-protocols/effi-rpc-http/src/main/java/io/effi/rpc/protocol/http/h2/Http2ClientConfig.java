package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.option.Options;

/**
 * Implements {@link ClientConfig} using http2.
 */
public class Http2ClientConfig extends Http2EndpointConfig implements ClientConfig {

    private static final Http2ClientConfig DEFAULT_CONFIG = builder().id("default-http2").build();

    Http2ClientConfig(String id, Options options, CertificateConfig certificateConfig) {
        super(id, options, certificateConfig);
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
    public static class Builder extends EndpointConfig.Builder<Http2ClientConfig, Builder>
            implements Http2EndpointConfig.Configurator<Http2ClientConfig.Builder> {

        @Override
        public Http2ClientConfig build() {
            return new Http2ClientConfig(id, options, certificateConfig);
        }
    }
}
