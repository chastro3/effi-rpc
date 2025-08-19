package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.config.Config;
import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.protocol.http.HttpClientConfig;
import io.effi.rpc.protocol.http.HttpClientConfigBuilder;

import static io.effi.rpc.config.ConfigValues.Protocol.HTTP_1_1;

/**
 * Implements {@link ClientConfig} using http1.1.
 */
public class Http1ClientConfig extends HttpClientConfig {

    private static final Http1ClientConfig DEFAULT_CONFIG = builder().id("default-htt1").build();

    Http1ClientConfig(String id, Config config, CertificateConfig certificateConfig) {
        super(id, config, HTTP_1_1, certificateConfig);
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

        @Override
        public Http1ClientConfig build() {
            return new Http1ClientConfig(id, config, certificateConfig);
        }
    }
}
