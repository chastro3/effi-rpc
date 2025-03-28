package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.engine.AbstractNamedConfig;

/**
 * Configuration class for HTTP/2 server settings.
 */
public class Http2ServerConfig extends AbstractNamedConfig implements ServerConfig {
    Http2ServerConfig(String protocol, String name, Config config) {
        super(protocol, name, config);
    }

    /**
     * Returns a default {@link Http2ServerConfig} instance with default settings.
     *
     * @return default {@link Http2ServerConfig} instance
     */
    public static Http2ServerConfig defaultConfig() {
        return builder().build();
    }

    /**
     * Creates and returns a new {@link Http2ServerConfigBuilder} instance for constructing
     * {@link Http2ServerConfig} objects using a fluent API.
     *
     * @return
     */
    public static Http2ServerConfigBuilder builder() {
        return new Http2ServerConfigBuilder();
    }
}
