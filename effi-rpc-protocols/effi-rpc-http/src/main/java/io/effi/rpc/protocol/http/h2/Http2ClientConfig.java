package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.config.Config;
import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.protocol.http.HttpClientConfig;
import io.effi.rpc.protocol.http.HttpClientConfigBuilder;

import static io.effi.rpc.config.ConfigValues.Protocol.HTTP_2;

/**
 * Implements {@link ClientConfig} using http2.
 */
public class Http2ClientConfig extends HttpClientConfig {

    private static final Http2ClientConfig DEFAULT_CONFIG = builder().id("default-http2").build();

    Http2ClientConfig(String id, Config config, CertificateConfig certificateConfig) {
        super(id, config, HTTP_2, certificateConfig);
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

        @Override
        public Http2ClientConfig build() {
            return new Http2ClientConfig(id, config, certificateConfig);
        }
    }
}
