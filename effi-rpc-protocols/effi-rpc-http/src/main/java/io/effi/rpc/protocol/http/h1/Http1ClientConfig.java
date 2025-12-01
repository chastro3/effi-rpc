package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.config.IdentifiableConfig;
import io.effi.rpc.config.Options;

/**
 * Implements {@link ClientConfig} using http1.1.
 */
public class Http1ClientConfig extends Http1EndpointConfig implements ClientConfig {

    private static final Http1ClientConfig DEFAULT_CONFIG = builder().id("default-htt1").build();

    Http1ClientConfig(String id, Options options) {
        super(id, options);
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
    public static class Builder extends IdentifiableConfig.Builder<Http1ClientConfig, Builder>
            implements Http1EndpointConfig.Configurator<Builder> {

        @Override
        public Http1ClientConfig build() {
            return new Http1ClientConfig(id, options);
        }
    }
}
