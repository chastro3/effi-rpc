package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.support.AbstractNamedConfig;

/**
 * Configuration class for HTTP/2 client settings.
 */
public class Http2ClientConfig extends AbstractNamedConfig implements ClientConfig {

    private static final Http2ClientConfig DEFAULT_CONFIG = builder().name("default-http2").build();

    Http2ClientConfig(String protocol, String name, Config config) {
        super(protocol, name, config);
    }

    /**
     * Creates and returns a new {@link Http2ClientConfig} instance with default settings.
     *
     * @return New {@link Http2ClientConfig} instance with default settings
     */
    public static Http2ClientConfig defaultConfig() {
        return DEFAULT_CONFIG;
    }

    /**
     * Creates and returns a new {@link Http2ClientConfigBuilder} instance for constructing
     * {@link Http2ClientConfig} objects using a fluent API.
     *
     * @return
     */
    public static Http2ClientConfigBuilder builder() {
        return new Http2ClientConfigBuilder();
    }
}
