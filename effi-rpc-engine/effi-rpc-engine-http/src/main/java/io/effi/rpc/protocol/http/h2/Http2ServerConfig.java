package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.Config;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.engine.AbstractNamedConfig;

/**
 * Configuration class for HTTP/2 server settings.
 */
public class Http2ServerConfig extends AbstractNamedConfig implements ServerConfig {
    Http2ServerConfig(String protocol, String name, Config config) {
        super(protocol, name, config);
    }

    public static Http2ServerConfig defaultConfig() {
        return builder().build();
    }

    public static Http2ServerConfigBuilder builder() {
        return new Http2ServerConfigBuilder();
    }
}
